package kr.co.inhatc.inhatc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.co.inhatc.inhatc.entity.FollowEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    // 팔로우 관계 확인
    Optional<FollowEntity> findByFollowerAndFollowing(MemberEntity follower, MemberEntity following);

    // 특정 사용자를 팔로우하는 사람들 (팔로워 목록)
    List<FollowEntity> findByFollowing(MemberEntity following);

    // 특정 사용자가 팔로우하는 사람들 (팔로잉 목록)
    List<FollowEntity> findByFollower(MemberEntity follower);

    // 팔로워 수 조회
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.following = :member")
    Long countFollowersByFollowing(@Param("member") MemberEntity member);

    // 팔로잉 수 조회
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.follower = :member")
    Long countFollowingByFollower(@Param("member") MemberEntity member);

    // 팔로우 여부 확인
    boolean existsByFollowerAndFollowing(MemberEntity follower, MemberEntity following);

    // 팔로워가 특정 이메일 목록 중 누구를 팔로우하는지 일괄 조회
    @Query("SELECT f.following.memberEmail FROM FollowEntity f WHERE f.follower = :follower AND f.following.memberEmail IN :emails")
    List<String> findFollowingEmails(@Param("follower") MemberEntity follower, @Param("emails") List<String> emails);
}




