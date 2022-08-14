package com.hanw.community.event;

import com.alibaba.fastjson.JSONObject;
import com.hanw.community.entity.Event;
import com.hanw.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author hanW
 * @create 2022-08-13 18:07
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    //触发事件
    public void fireEvent(Event event){
        //将事件发送到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
