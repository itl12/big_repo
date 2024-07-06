from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.contrib.auth.decorators import login_required
from .models import Cart
from App_Shop.models import Product

# Create your views here.

@login_required
def add_to_cart(request, pk):
    item = get_object_or_404(Product, pk=pk)
    cart, created = Cart.objects.get_or_create(user=request.user, item=item, purchased=False)
    if created:
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
    

