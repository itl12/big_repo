from App_Cart.models import Order 


def item_count(request):
    no_of_carts = 0
    if request.user.is_authenticated:
        items = Order.objects.filter(user=request.user, ordered=False)
        if items:
            items = items[0]
            no_of_carts = items.orderItems.count()
            return { 'no_of_carts': no_of_carts }
        
    return { 'no_of_carts': no_of_carts }