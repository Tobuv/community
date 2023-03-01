package com.toubv.community;

import com.toubv.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSend(){
        mailClient.sendMail("2360737675@qq.com", "test", "send test");
    }

    @Test
    public void testSendHtml(){
        Context context = new Context();
        context.setVariable("username", "ZACK");
        String content = templateEngine.process("/mail/demo", context);

        mailClient.sendMail("2360737675@qq.com", "test", content);

    }

}
