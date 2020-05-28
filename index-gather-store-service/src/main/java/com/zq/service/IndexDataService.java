package com.zq.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.zq.pojo.IndexData;
import com.zq.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataService {
    private Map<String, List<IndexData>> indexDatasMap = new HashMap<>();
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    IndexDataService indexDataService;

    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<IndexData> fresh(String code) {
        List<IndexData> indexDatas = fetch_indexes_from_third_part(code);
        indexDatasMap.put(code, indexDatas);
        SpringContextUtil.getBean(IndexDataService.class);
        indexDataService.remove(code);
        return indexDataService.store(code);
    }

    @CachePut(key = "'indexData-code-'+#p0")
    public List<IndexData> store(String code) {
        return indexDatasMap.get(code);
    }

    @Cacheable(key = "'indexData-code-'+#p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }

    @CacheEvict(key = "'indexData-code-'+#p0")
    public void remove(String code) {

    }

    public List<IndexData> fetch_indexes_from_third_part(String code) {
        List<Map> temp = restTemplate.getForObject("http://127.0.0.1:8090/indexes/" + code + ".json", List.class);
        return map2IndexData(temp);
    }

    public List<IndexData> third_part_not_connected(String code) {
        System.out.println("third_part_not_connected()");
        IndexData indexData = new IndexData();
        indexData.setDate("n/a");
        indexData.setClosePoint(0F);
        return CollectionUtil.toList(indexData);
    }

    public List<IndexData> map2IndexData(List<Map> temp) {
        List<IndexData> indexDatas = new ArrayList<>();
        for (Map map : temp) {
            IndexData indexData = new IndexData();
            indexData.setDate(map.get("date").toString());
            indexData.setClosePoint(Convert.toFloat(map.get("closePoint").toString()));
            indexDatas.add(indexData);
        }
        return indexDatas;
    }
}
