var user = user || {};

user.users_list = {
    UsersList: function () {
        var self = this
        self.users = ko.observableArray([]);

        $.getJSON("/api/users", function (data) {
            self.users(data);
        });
    }
};

$(function() {
    var model = document.getElementById("users-list");
    if (model) {
        ko.applyBindings(new user.users_list.UsersList(), model);
    }
});