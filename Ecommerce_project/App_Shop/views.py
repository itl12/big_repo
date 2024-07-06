import logging
from typing import Any
from django.db.models.base import Model as Model
from django.db.models.query import QuerySet
from django.shortcuts import render, redirect, get_object_or_404
from django.views.generic import DetailView
from .models import Product
from django.http import Http404


# Create your views here.


class product_details(DetailView):
    model = Product 
    template_name = 'product_detail.html'
    context_object_name = 'product'
   
    def get_context_data(self, **kwargs: Any) -> dict[str, Any]:
        context = super().get_context_data(**kwargs)
        context['title'] = self.object.name
        return context
    
