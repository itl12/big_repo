from django.shortcuts import render, redirect
from .forms import BillingAddressForm
from .models import BillingAddress
from App_Cart.models import Cart, Order
from django.contrib import messages
from django.contrib.auth.decorators import login_required
from sslcommerz_lib import SSLCOMMERZ 

# Create your views here.

@login_required
def checkout(request):
    billingAddress, created = BillingAddress.objects.get_or_create(user=request.user)
    order, create = Order.objects.get_or_create(user=request.user, ordered=False)
    if request.method == 'POST':
        form = BillingAddressForm(data=request.POST, instance=billingAddress)
        if form.is_valid():
            form.save()
        return render(request, 'checkout.html', {'form': form, 'order': order  })
    else:
        form = BillingAddressForm(instance=billingAddress)
        return render(request, 'checkout.html', {'form': form, 'order': order })
    


@login_required
def payment(request):
    settings = { 'store_id': 'testbox', 'store_pass': 'qwerty', 'issandbox': True }
    sslcz = SSLCOMMERZ(settings)
