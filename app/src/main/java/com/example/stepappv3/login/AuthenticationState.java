package com.example.stepappv3.login;

/**
 * Represents the different states of user authentication
 * for the UI to observe.
 */
public enum AuthenticationState {
    AUTHENTICATED,      // The user is successfully signed in.
    UNAUTHENTICATED,    // The user is signed out or the sign-in failed.
    IN_PROGRESS         // A sign-in flow is currently in progress.
}