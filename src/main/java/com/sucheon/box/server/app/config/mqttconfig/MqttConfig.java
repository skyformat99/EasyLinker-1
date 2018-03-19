package com.sucheon.box.server.app.config.mqttconfig;

import com.sucheon.box.server.app.config.mqttconfig.handler.ClientOnAndOfflineWillMessageHandler;
import com.sucheon.box.server.app.config.mqttconfig.handler.InMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;

/**
 * Created by wwhai on 2018/3/14.
 */
@Configuration
public class MqttConfig {
    private static final String LOCALHOST_EMQ_URL = "tcp://localhost";
    private static final String LOCALHOST_EMQ_USERNAME = "localhost";
    private static final String LOCALHOST_EMQ_PASSWORD = "localhost";
    Logger logger = LoggerFactory.getLogger(MqttConfig.class);


    /**
     * mqtt 的工厂  用来创建mqtt连接
     *
     * @return
     */

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(LOCALHOST_EMQ_URL);
        factory.setUserName(LOCALHOST_EMQ_USERNAME);
        factory.setPassword(LOCALHOST_EMQ_PASSWORD);
        return factory;
    }

    /**
     * 监控设备盒子上下线
     *
     * @return
     */
    @Bean("MqttClientOnOrOffLineMessageListenerInbound")
    public MessageProducerSupport getMqttClientOnOrOffLineMessageListener() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "MqttClientOnOrOffLineMessageListenerInbound",
                mqttClientFactory());
        adapter.addTopic("$SYS/brokers/+/clients/+/#");//监控设备消息上下线
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }


    /**
     * 监控设备盒子上下线发进来的消息监听器
     *
     * @return
     */
    @Bean("MqttClientOnOrOffLineMessageListener")
    public IntegrationFlow mqttClientOnOrOffLineMessageListenerInFlow() {
        return IntegrationFlows.from(getMqttClientOnOrOffLineMessageListener())
                .handle(new ClientOnAndOfflineWillMessageHandler())
                .get();
    }


    /**
     * 盒子发进来的消息消息接收
     *
     * @return
     */
    @Bean("MqttClientInMessageListenerInbound")
    public MessageProducerSupport getMqttClientInMessageListener() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "MqttClientInMessageListenerInbound",
                mqttClientFactory());
        //in/client/{GROUP}/{OPENID}  为客户端SUB的TOPIC
        adapter.addTopic("in/client/+/#");//监控设备publish的消息
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;

    }


    /**
     * 监控设备盒子发进来的消息接受处理器
     *
     * @return
     */
    @Bean("MqttClientInMessageListenerInFlow")
    public IntegrationFlow mqttClientInMessageListenerInFlow() {
        return IntegrationFlows.from(getMqttClientInMessageListener())
                .handle(new InMessageHandler())
                .get();
    }
}