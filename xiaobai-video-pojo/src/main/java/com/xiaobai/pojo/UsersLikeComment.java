package com.xiaobai.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "users_like_comment")
public class UsersLikeComment {


        @Id
        private String id;

        /**
         * 用户
         */
        @Column(name = "user_id")
        private String userId;

        /**
         * 评论
         */
        @Column(name = "comment_id")
        private String commentId;

        /**
         * @return id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 获取用户
         *
         * @return user_id - 用户
         */
        public String getUserId() {
            return userId;
        }

        /**
         * 设置用户
         *
         * @param userId 用户
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }
}
