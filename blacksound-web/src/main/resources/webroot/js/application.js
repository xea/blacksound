let _ = new Vue({
    el: '#app',
    data: {
        message: "Vue is working"
    },
    methods: {
        nextSong: function() {
            fetch("/api/next-song", { method: "POST" })
                .then(function(response) {
                    console.log(response);
                })
        }
    }

});