package org.kosa.shoppingmaillmanager.member;


import jakarta.persistence.*;
import lombok.*;
import org.kosa.shoppingmaillmanager.member.Member;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_live_broadcasts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Broadcast {

    @Id
   
    @Column(name = "broadcast_id")
    private String broadcastId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcaster_id", referencedColumnName = "USER_ID")
    private Member broadcaster;

    private String title;
    private String description;

    @Column(name = "broadcast_status")
    private String broadcastStatus;

    @Column(name = "scheduled_start_time")
    private Timestamp scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private Timestamp scheduledEndTime;

    @Column(name = "actual_start_time")
    private Timestamp actualStartTime;

    @Column(name = "actual_end_time")
    private Timestamp actualEndTime;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "max_viewers")
    private Integer maxViewers;

    @Column(name = "current_viewers")
    private Integer currentViewers;

    @Column(name = "total_viewers")
    private Integer totalViewers;

    @Column(name = "peak_viewers")
    private Integer peakViewers;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "category_id")
    private Integer categoryId;

    private String tags;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "stream_key")
    private String streamKey;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "obs_host")
    private String obsHost;

    @Column(name = "obs_port")
    private Integer obsPort;

    @Column(name = "obs_password")
    private String obsPassword;

    @Column(name = "nginx_host")
    private String nginxHost;
}
