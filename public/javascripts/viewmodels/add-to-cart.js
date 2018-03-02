var product = product || {};

product.add_to_cart = {
    AddToCartForm: function (userId) {
        var self = this;

        self.addToCart = function () {
            $.ajax({
                url: "/product/buy/" + userId,
                type: 'POST'
            }).done(function () {
                window.location.replace("/profile/products");
            });
        }
    }
};

$(function() {
    var model = document.getElementById("product-detail-form");
    if (model) {
        ko.applyBindings(new product.add_to_cart.AddToCartForm(model.getAttribute('data-userId')), model);
    }
});