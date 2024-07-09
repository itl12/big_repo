from django.shortcuts import render, redirect, get_object_or_404
from django.urls import reverse
from django.contrib import messages
from django.contrib.auth.decorators import login_required
from .models import Cart, Order
from App_Shop.models import Product

# Create your views here.

@login_required
def add_to_cart(request, pk):
    item = get_object_or_404(Product, pk=pk)
    cart, created = Cart.objects.get_or_create(user=request.user, item=item, purchased=False)
    if created:
        order, created = Order.objects.get_or_create(user=request.user, ordered=False)
        order.orderItems.add(cart)
        order.save()
        messages.success(request, 'Item added to cart!')
        return redirect('/')
    else:
        cart.quantity += 1
        cart.save()
        messages.success(request, 'Item quantity updated successfully!')
        return redirect('/')



@login_required
def cart(request):
    carts = Cart.objects.filter(user=request.user, purchased=False)
    if carts:
        return render(request, 'cart.html', {'title': 'Cart', 'carts': carts})
    else:
        messages.info(request, 'You don\'t have anything in cart.')
        return redirect('/')
    

@login_required
def remove_from_cart(request, pk):
    cart = get_object_or_404(Cart, pk=pk, user=request.user, purchased=False)
    cart.delete()
    messages.success(request, 'Removed item!')
    return redirect('App_Cart:cart')


@login_required
def decrease_item(request, pk):
    cart = Cart.objects.filter(pk=pk, user=request.user, purchased=False)
    if cart:
        cart = cart[0]
        if cart.quantity > 1:
            cart.quantity -= 1
            cart.save()
            messages.success(request, 'Item quantity updated!')
            return redirect('App_Cart:cart')
        else:
            return redirect(reverse('App_Cart:remove_from_cart', kwargs={'pk':cart.pk}))
        

@login_required
def increase_item(request, pk):
    cart = Cart.objects.filter(pk=pk, user=request.user, purchased=False)
    if cart:
        cart = cart[0]
        cart.quantity += 1
        cart.save()
        messages.success(request, 'Item quantity updated!')
        return redirect('App_Cart:cart')
