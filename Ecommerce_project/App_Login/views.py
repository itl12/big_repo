from django.shortcuts import render, redirect, HttpResponseRedirect
from django.contrib.auth import authenticate, login, logout
from django.contrib import messages
from django.urls import reverse
from .forms import SignupForm, EditProfileForm
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.auth.decorators import login_required
from .models import Profile

# Create your views here.
def user_signup(request):
    
    if request.method == 'POST':
        form = SignupForm( data=request.POST )
        if form.is_valid():
            form.save()
            messages.success(request, 'Account created successfully!')
            return redirect('App_Login:login')
            
    else:
        form = SignupForm() 
    
    return render(request, 'signup.html', {'title': 'Signup', 'form': form})

def user_login(request):
    
    if request.method == 'POST':
        form = AuthenticationForm(request, data=request.POST)
        if form.is_valid():
            email = request.POST['username']
            password = request.POST['password'] 
            user = authenticate(request, username=email, password=password)
            if user is not None:
                login(request, user)
                messages.success(request, 'Login successfull!')
                return redirect('/')
    else:
        form = AuthenticationForm()
    
    return render(request, 'login.html', {'title': 'Login', 'form': form})



@login_required
def user_logout(request):
    logout(request)
    messages.warning(request, 'Logged out!') 
    return redirect('/')


@login_required
def edit_profile(request):
    user = Profile.objects.get(user=request.user)
    if request.method == 'POST':
        form = EditProfileForm( data=request.POST, instance=user )
        if form.is_valid():
            profile = form.save(commit=False)
            profile.user = request.user
            profile.save()
            messages.success(request, 'Profile updated.')
            return redirect('/')
    else:
        form = EditProfileForm(instance=user)
        return render(request, 'edit_profile.html', {'title': 'Edit Profile', 'form': form })