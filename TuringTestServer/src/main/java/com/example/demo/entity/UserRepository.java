package com.example.demo.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    List<User> findByUsername(String username);
    
    List<User> findByUid(long id);

    <S extends User> S save(S entity);
    @Transactional
    @Modifying
    @Query(value = "CREATE TEMPORARY TABLE ranked_users AS\r\n" + //
            "SELECT \r\n" + //
            "    uid,\r\n" + //
            "    username,\r\n" + //
            "    score,\r\n" + //
            "    @m := @m + 1 `rank`\r\n" + //
            "FROM\r\n" + //
            "    user,\r\n" + //
            "    (SELECT @m:=0)a\r\n" + //
            "    where is_delete=0\r\n" + //
            "ORDER BY score DESC;",nativeQuery = true)
    void createRankedUsers();

    @Query(value = "SELECT `rank`\r\n" + //
            "FROM ranked_users \r\n" + //
            "WHERE uid = ?1;",nativeQuery = true)
    Long getRank(Long uid);
    
    @Query(value = 
            "SELECT username, `rank`, score \r\n" + //
            "FROM ranked_users \r\n" + //
            "WHERE `rank` BETWEEN ?1 - 2 AND ?1 + 2;\r\n",nativeQuery = true)
    List<Map<String,String>> getRankingNearUser(Long rank);

    @Transactional
    @Modifying
    @Query(value = "DROP TEMPORARY TABLE ranked_users;",nativeQuery = true)
    void dropRankedUsers();

    @Query(value = "select username,@m1:=@m1+1 `rank`, score from user,(select @m1:=0)a where is_delete=0 order by score desc limit 5;",nativeQuery = true)
    List<Map<String,String>> getTopRankingOrderByScoreDesc();

    @Transactional
    @Modifying
    @Query(value = "update user set score = ?2 where uid=?1;",nativeQuery = true)
    void updateScoreByUid(Long uid, Long score);

    @Query(value = "select score from user where uid=?1;",nativeQuery = true)
    Long getScoreByUid(Long uid);

}
