import React, { useState } from "react";
import { auth, googleProvider, signInWithPopup, db, doc, setDoc, serverTimestamp } from "../firebase";

export default function Login({ onUserAuthenticated }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGoogleSignIn = async () => {
    setLoading(true);
    setError(null);

    try {
      // 1. Trigger Google Popup Authentication
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;

      // 2. Extract profile details
      const userProfileData = {
        uid: user.uid,
        displayName: user.displayName || "KevSpace Explorer",
        email: user.email || "",
        photoURL: user.photoURL || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.displayName || "User")}&background=6366F1&color=fff`,
        status: "online",
        lastSeen: serverTimestamp(),
        updatedAt: serverTimestamp()
      };

      // 3. Save or update user profile document in Firestore 'users' collection
      await setDoc(doc(db, "users", user.uid), userProfileData, { merge: true });

      if (onUserAuthenticated) {
        onUserAuthenticated(user);
      }
    } catch (err) {
      console.error("KevSpace Login Error:", err);
      setError(err.message || "Failed to sign in with Google. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-[#0F172A] via-[#1E1B4B] to-[#020617] px-4 py-8 text-white font-sans antialiased">
      <div className="relative w-full max-w-md bg-slate-900/80 backdrop-blur-xl border border-indigo-500/20 rounded-3xl p-8 sm:p-10 shadow-2xl shadow-indigo-950/50 flex flex-col items-center text-center overflow-hidden">
        {/* Decorative Glow */}
        <div className="absolute -top-24 -left-24 w-48 h-48 bg-indigo-500/30 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -right-24 w-48 h-48 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

        {/* KevSpace Cosmic Logo Emblem */}
        <div className="relative z-10 mb-6 flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-cyan-400 p-0.5 shadow-lg shadow-indigo-500/30 animate-pulse">
          <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
            <svg
              className="w-10 h-10 text-cyan-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
          </div>
        </div>

        {/* Branding & Welcome */}
        <h1 className="relative z-10 text-3xl sm:text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-100 to-indigo-200">
          Welcome to KevSpace
        </h1>

        <p className="relative z-10 mt-3 text-sm sm:text-base text-slate-400 max-w-xs leading-relaxed">
          Real-time dynamic messaging, live presence, and secure cloud synchronization.
        </p>

        {/* Error notification */}
        {error && (
          <div className="relative z-10 mt-5 w-full bg-red-500/10 border border-red-500/30 text-red-300 text-xs sm:text-sm rounded-xl p-3 text-left">
            <p className="font-semibold">Authentication Notice</p>
            <p className="mt-0.5">{error}</p>
          </div>
        )}

        {/* Google Popup Sign-In Action */}
        <div className="relative z-10 mt-8 w-full">
          <button
            onClick={handleGoogleSignIn}
            disabled={loading}
            className="w-full flex items-center justify-center gap-3.5 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-medium py-3.5 px-6 rounded-xl transition duration-200 ease-in-out shadow-lg shadow-indigo-600/30 hover:shadow-indigo-500/40 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
          >
            {loading ? (
              <div className="flex items-center gap-2">
                <svg
                  className="animate-spin h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                <span>Connecting to KevSpace...</span>
              </div>
            ) : (
              <>
                <div className="w-6 h-6 bg-white rounded-full flex items-center justify-center p-1 shadow-sm">
                  <svg className="w-full h-full" viewBox="0 0 24 24">
                    <path
                      fill="#4285F4"
                      d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.9c2.28-2.1 3.64-5.2 3.64-9.15z"
                    />
                    <path
                      fill="#34A853"
                      d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.9-3.05c-1.08.72-2.45 1.16-4.03 1.16-3.1 0-5.73-2.1-6.66-4.93H1.28v3.13C3.25 21.27 7.31 24 12 24z"
                    />
                    <path
                      fill="#FBBC05"
                      d="M5.34 14.27c-.24-.72-.38-1.49-.38-2.27s.14-1.55.38-2.27V6.6H1.28C.46 8.23 0 10.06 0 12s.46 3.77 1.28 5.4l4.06-3.13z"
                    />
                    <path
                      fill="#EA4335"
                      d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.25 2.73 1.28 6.6l4.06 3.13c.93-2.83 3.56-4.98 6.66-4.98z"
                    />
                  </svg>
                </div>
                <span className="font-semibold text-slate-100">Sign in with Google</span>
              </>
            )}
          </button>
        </div>

        {/* Feature Badges */}
        <div className="relative z-10 mt-10 pt-6 border-t border-slate-800/80 w-full flex items-center justify-between text-xs text-slate-400">
          <div className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
            <span>Live Firestore</span>
          </div>
          <div>•</div>
          <div>Real-time sync</div>
          <div>•</div>
          <div>Zero mock data</div>
        </div>
      </div>
    </div>
  );
}
