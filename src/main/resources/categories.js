function populateList() {
    var category = list.data("category");
    $.get("/grumblr/get-category/"+category)
      .done(function(data) {
          var list = $("#categoryhere");
          list.data('max-time', data['max-time']);
          list.html('');
          for (var i = 0; i < data.posts.length; i++) {
              post = data.posts[i];
              var new_post = $(post.html);
              new_post.data("post-id", post.id);
              list.prepend(new_post);
              $("#comment-button-"+post.id).data("post-id",post.id);
              $.get("/grumblr/get-comments/"+post.id)
                  .then(function (subdata) {
                      var commentShow = $("#comment-show-"+subdata.post_id);
                      for(var j = 0; j<subdata.comments.length;j++){
                          comment = subdata.comments[j];
                          var newComment = $(comment.html);
                          newComment.data("comment-id",comment.id);
                          commentShow.append(newComment);
                      }
                  });
          }
          $(".commentNow").on("click",addComment);
      });
}

function addComment(event){
    var post_id = $(event.target).data("post-id");
    var commentContent = $("#comment-content-"+post_id);
    $.post("/grumblr/add-comments/"+post_id,{"comment":commentContent.val()})
        .done(function (data) {
            populateList();
        });
}

function getUpdates() {
    var list = $("#categoryhere");
    var max_time = list.data("max-time");
    var category = list.data("category");
    $.get("/grumblr/get-category/"+category+"/"+max_time)
      .done(function(data) {
          list.data('max-time', data['max-time']);
          for (var i = 0; i < data.posts.length; i++) {
              var post = data.posts[i];
              var new_post = $(post.html);
              list.prepend(new_post);
              var button = $("#comment-button-"+post.id);
              button.data("post-id",post.id);
              button.on("click",addComment);
          }
      });
}

$(document).ready(function(){
    // Periodically refresh to-do list
    populateList();
    window.setInterval(getUpdates, 5000);
    // CSRF set-up copied from Django docs
    function getCookie(name) {
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }

    var csrftoken = getCookie('csrftoken');
    $.ajaxSetup({
        beforeSend: function (xhr, settings) {
            xhr.setRequestHeader("X-CSRFToken", csrftoken);
        }
    });
});