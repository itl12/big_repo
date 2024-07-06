from django.urls import reverse
from django.shortcuts import render, redirect, HttpResponseRedirect
from App_Shop.models import Product, Updates



def home(request):
    items = Product.objects.all()
    return render(request, 'home.html', {'title': 'Home', 'items': items})