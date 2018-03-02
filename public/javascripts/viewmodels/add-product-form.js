var product = product || {};

product.add_product_form = {
    AddProductForm: function () {
        var self = this;
        self.name = ko.observable("");
        self.description = ko.observable("");
        self.price = ko.observable("");

        self.submitForm = function (form) {
            var formData = new FormData($("#add-product-form")[0]);
            $.ajax({
                url: "/product/add",
                type: 'POST',
                headers: { 'IsAjax': 'true' },
                processData: false,
                contentType: false,
                data: formData,
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            }).done(function () {
                window.location.replace("/profile/products");
            });
        }
    }
};

function readUrl(input) {
    if(input.files && input.files[0]){
        var reader = new FileReader();
        reader.onload = function (ev) {
            $('#productImg').attr('src', ev.target.result);
        };
        reader.readAsDataURL(input.files[0]);
    }
}
$("#file").change(function () {
    readUrl(this);
});

$(function() {
    var model = document.getElementById("add-product-form");
    if (model) {
        ko.applyBindings(new product.add_product_form.AddProductForm(), model);
    }
});