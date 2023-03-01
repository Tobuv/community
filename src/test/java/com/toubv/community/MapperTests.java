package com.toubv.community;

import com.toubv.community.dao.DiscussPostMapper;
import com.toubv.community.dao.LoginTicketMapper;
import com.toubv.community.dao.MessageMapper;
import com.toubv.community.dao.UserMapper;
import com.toubv.community.entity.DiscussPost;
import com.toubv.community.entity.LoginTicket;
import com.toubv.community.entity.Message;
import com.toubv.community.entity.User;
import com.toubv.community.service.DiscussPostService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.crypto.spec.PSource;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println("user = " + user);

        user = userMapper.selectByName("liubei");
        System.out.println("user = " + user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println("user = " + user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int row = userMapper.insertUser(user);
        System.out.println("row = " + row);
        System.out.println("user.getId() = " + user.getId());
    }

    @Test
    public void testUpdate(){
        int row = userMapper.updateStatus(150, 1);
        System.out.println("row = " + row);

        row = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println("row = " + row);

        row = userMapper.updatePassword(150, "654321");
        System.out.println("row = " + row);
    }

    @Test
    public void testSelectPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(101, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println("discussPost = " + discussPost);
        }
        System.out.println("==================================");
        int rows = discussPostMapper.selectDiscussPostRows(101);
        System.out.println("rows = " + rows);
    }

    @Test
    public void testInsertTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(103);
        loginTicket.setTicket("abcd");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(loginTicket.getId());
    }

    @Test
    public void testSelectAndUpdateTicket(){
        LoginTicket ticket = loginTicketMapper.selectByTicket("abcd");
        System.out.println(ticket);
        loginTicketMapper.updateStatus(ticket.getTicket(), 1);
        ticket = loginTicketMapper.selectByTicket("abcd");
        System.out.println(ticket);
    }

    @Test
    public void testInsertPost(){
        DiscussPost post = new DiscussPost();
        post.setUserId(101);
        post.setTitle("Hello,World");
        post.setContent("Test Hello , world");
        post.setCreateTime(new Date());


        discussPostMapper.insertDiscussPost(post);
        System.out.println(post.getId());
    }

    @Test
    public void testSelectLetter(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 20);
        for (Message letter : letters) {
            System.out.println(letter);
        }
        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println(letterCount);

        int letterUnreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(letterUnreadCount);
    }

}
