package com.sequenceiq.datalake.service.sdx;

import static com.sequenceiq.cloudbreak.common.exception.NotFoundException.notFound;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dyngr.Polling;
import com.dyngr.core.AttemptResults;
import com.sequenceiq.cloudbreak.common.json.JsonUtil;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.repository.SdxClusterRepository;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.DatabaseServerV4Endpoint;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.requests.AllocateDatabaseServerV4Request;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.responses.DatabaseServerStatusV4Response;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.responses.DatabaseServerTerminationOutcomeV4Response;
import com.sequenceiq.redbeams.api.model.common.Status;

@Service
public class DatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final int SLEEP_TIME_IN_SEC_FOR_ENV_POLLING = 10;

    private static final int DURATION_IN_MINUTES_FOR_ENV_POLLING = 60;

    @Inject
    private SdxClusterRepository sdxClusterRepository;

    @Inject
    private DatabaseServerV4Endpoint databaseServerV4Endpoint;

    public DatabaseServerStatusV4Response create(Long sdxId, DetailedEnvironmentResponse env) {
        DatabaseServerStatusV4Response resp = databaseServerV4Endpoint.create(getDatabaseRequest(env));
        DatabaseServerStatusV4Response waitResp = waitAndGetDatabase(sdxId, resp.getResourceCrn(),
                status -> status.isAvailable(), status -> Status.CREATE_FAILED.equals(status));
        sdxClusterRepository.findById(sdxId).ifPresent(sdxCluster -> {
            sdxCluster.setDatabaseCrn(waitResp.getResourceCrn());
            sdxClusterRepository.save(sdxCluster);
        });
        return waitResp;
    }

    public void terminate(Long sdxId) {
        sdxClusterRepository.findById(sdxId).ifPresent(sdxCluster -> {
            DatabaseServerTerminationOutcomeV4Response resp = databaseServerV4Endpoint.terminate(sdxCluster.getDatabaseCrn());
            waitAndGetDatabase(sdxId, resp.getResourceCrn(),
                    status -> Status.DELETE_COMPLETED.equals(status), status -> Status.DELETE_FAILED.equals(status));
        });
    }

    private AllocateDatabaseServerV4Request getDatabaseRequest(DetailedEnvironmentResponse env) {
        AllocateDatabaseServerV4Request req = new AllocateDatabaseServerV4Request();
        req.setEnvironmentCrn(env.getCrn());
        return req;
    }

    public DatabaseServerStatusV4Response waitAndGetDatabase(Long sdxId, String databaseCrn,
        Function<Status, Boolean> exitcrit, Function<Status, Boolean> failurecrit) {
        PollingConfig pollingConfig = new PollingConfig(SLEEP_TIME_IN_SEC_FOR_ENV_POLLING, TimeUnit.SECONDS,
                DURATION_IN_MINUTES_FOR_ENV_POLLING, TimeUnit.MINUTES);
        return waitAndGetDatabase(sdxId, databaseCrn, pollingConfig, exitcrit, failurecrit);
    }

    public DatabaseServerStatusV4Response waitAndGetDatabase(Long sdxId, String databaseCrn, PollingConfig pollingConfig,
            Function<Status, Boolean> exitcrit, Function<Status, Boolean> failurecrit) {
        Optional<SdxCluster> sdxClusterOptional = sdxClusterRepository.findById(sdxId);
        if (sdxClusterOptional.isPresent()) {
            SdxCluster sdxCluster = sdxClusterOptional.get();
            DatabaseServerStatusV4Response databaseResponse = Polling.waitPeriodly(pollingConfig.getSleepTime(), pollingConfig.getSleepTimeUnit())
                    .stopIfException(false)
                    .stopAfterDelay(pollingConfig.getDuration(), pollingConfig.getDurationTimeUnit())
                    .run(() -> {
                        LOGGER.info("Creation polling redbeams for database status: '{}' in '{}' env",
                                sdxCluster.getClusterName(), sdxCluster.getEnvName());
                        DatabaseServerStatusV4Response rdsStatus = getDatabaseStatus(databaseCrn);
                        LOGGER.info("Response from redbeams: {}", JsonUtil.writeValueAsString(rdsStatus));
                        if (exitcrit.apply(rdsStatus.getStatus())) {
                            return AttemptResults.finishWith(rdsStatus);
                        } else {
                            if (failurecrit.apply(rdsStatus.getStatus())) {
                                return AttemptResults.breakFor("Database creation failed " + sdxCluster.getEnvName());
                            } else {
                                return AttemptResults.justContinue();
                            }
                        }
                    });
            return databaseResponse;
        } else {
            throw notFound("SDX cluster", sdxId).get();
        }
    }

    private DatabaseServerStatusV4Response getDatabaseStatus(String databaseCrn) {
        return databaseServerV4Endpoint.getStatusOfManagedDatabaseServerByCrn(databaseCrn);
    }

}
