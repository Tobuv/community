package com.toubv.community;

import com.toubv.community.dao.DiscussPostMapper;
import com.toubv.community.dao.elasticsearch.DiscussPostRepository;
import com.toubv.community.entity.DiscussPost;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void test(){
        System.out.println(elasticsearchRestTemplate);
    }

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(287));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(288));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(289));
    }

    @Test
    public void testInertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }
    @Test
    public void testDelete(){
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                );
        NativeSearchQuery searchQuery = queryBuilder.build();

        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

        System.out.println(search.getTotalHits());


        for (SearchHit<DiscussPost> hit : search) {
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if(!highlightFields.isEmpty()){
                hit.getContent().setTitle(highlightFields.get("title") == null ? hit.getContent().getTitle() : highlightFields.get("title").get(0));
                hit.getContent().setContent(highlightFields.get("content") == null ? hit.getContent().getContent() : highlightFields.get("content").get(0));
            }
            System.out.println(hit);
        }

        System.out.println(search.getSearchHits().size());

//        search.forEach(hit ->{
//            System.out.println(hit.getContent());
//        });


    }





}
