package com.toubv.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.sun.javafx.binding.StringFormatter;
import com.toubv.community.common.constant.TopicConstant;
import com.toubv.community.entity.Event;
import com.toubv.community.entity.Message;
import com.toubv.community.service.DiscussPostService;
import com.toubv.community.service.ElasticsearchService;
import com.toubv.community.service.MessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventConsumer implements TopicConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private DiscussPostService discussPostService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${alibaba.cloud.access-key}")
    private String accessId;
    @Value("${alibaba.cloud.secret-key}")
    private String accessKey ;
    @Value("${alibaba.cloud.oss.bucketName}")
    private String bucketName ;
    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("?????????????????????");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("??????????????????");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_USER);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        Map<String, Object> map = new HashMap<>();
        map.put("userId", event.getUserId());
        map.put("entityId", event.getEntityId());
        map.put("entityType", event.getEntityType());

        if(!event.getMap().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));

        messageService.addMessage(message);
    }

    //??????????????????
    @KafkaListener(topics = TOPIC_PUBLISH)
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("?????????????????????");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("??????????????????");
            return;
        }

        elasticsearchService.saveDiscussPost(discussPostService.findDiscussPostById(event.getEntityId()));


    }

    //??????????????????
    @KafkaListener(topics = TOPIC_DELETE)
    public void handleDeleteMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("?????????????????????");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("??????????????????");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());


    }
    //??????????????????
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("?????????????????????");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("??????????????????");
            return;
        }

        String htmlUrl =(String) event.getMap().get("htmlUrl");
        String filename =(String) event.getMap().get("filename");
        String suffix =(String) event.getMap().get("suffix");

        logger.info("??????????????????");
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + filename + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("??????????????????" + cmd);
        } catch (IOException e) {
            logger.error("????????????????????? " + e.getMessage());
        }

        //?????????????????????????????????????????????????????????oss
        UploadTask task = new UploadTask(filename, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable{
        private String filename;
        private String suffix;
        private Future future;
        private long startTime;
        private int uploadTimes;

        public UploadTask(String filename, String suffix) {
            this.filename = filename;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            //??????????????????
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("?????????????????????????????????:" + filename);
                future.cancel(true);
                return;
            }
            //????????????
            if(uploadTimes >= 3){
                logger.error("?????????????????????????????????:" + filename);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + filename + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("?????????%d?????????[%s].", ++uploadTimes,filename));

                OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
                try {
                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, file);
                    putObjectRequest.setProcess("true");
                    PutObjectResult result = ossClient.putObject(putObjectRequest);

                    if(result == null || result.getResponse().getStatusCode() != 200){
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                    }else {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                        future.cancel(true);
                    }
                } catch (OSSException e) {
                    logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                } catch (ClientException e) {
                    logger.info(String.format("???%d???????????????[%s].", uploadTimes, filename));
                } finally {
                    if(ossClient != null){
                        ossClient.shutdown();
                    }
                }
            }
        }
    }

}
