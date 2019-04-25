## 关联查询

- 一对多 基础类型

  ```java
  public class ConsumerGroup extends BaseEntity {
      @TableField(exist = false)
      private List<Integer> topics = Lists.newArrayList();
  }
  ```

  ```java
  <!-- 通用查询映射结果 -->
  <resultMap id="BaseResultMap" type="com.ucarinc.nestle.webapp.entity.ConsumerGroup">
     <id column="id" property="id"/>
     ......
     <collection ofType="Integer" property="topics" >
        <result column="topic_id"/>
     </collection>
  </resultMap>
  ```

  ```sql
  <select id="selectConsumerGroupList" resultMap="BaseResultMap">
     select tcg.*,
           tcgt.topic_id as topic_id
     from t_consumer_group tcg
     left join t_consumer_group_topic tcgt on tcg.id=tcgt.consumer_group_id
     and tcg.is_delete = 0 and tcgt.is_delete = 0
     ${ew.customSqlSegment}
  </select>
  ```

- 

