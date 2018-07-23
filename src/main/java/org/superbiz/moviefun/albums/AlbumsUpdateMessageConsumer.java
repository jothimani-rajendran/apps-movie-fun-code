package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class AlbumsUpdateMessageConsumer {

    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AlbumsUpdateMessageConsumer(AlbumsUpdater albumsUpdater){
        this.albumsUpdater = albumsUpdater;
    }

    public void consume(Message<?> message){
        try {
            logger.debug("Starting albums update");
            albumsUpdater.update();
            logger.debug("Finished albums update");

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

}
