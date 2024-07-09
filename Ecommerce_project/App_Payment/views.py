from django.shortcuts import render, redirect
from .forms import BillingAddressForm
from .models import BillingAddress
from App_Cart.models import Cart, Order
from django.contrib import messages
from django.contrib.auth.decorators import login_required

# Create your views here.

@login_required
def checkout(request):
    carts = Carts.objects.filter(user=request.user, purchased=False)
    if request.method == 'POST':
        form = BillingAddressForm(request.POST)
        if form.is_valid():
            billing_address = form.save(commit=False)
            billing_address.user = request.user
            billing_address.save()

        return render(request, 'checkout.html', {'form': form })
    else:
        form = BillingAddressForm()
        return render(request, 'checkout.html', {'form': form})