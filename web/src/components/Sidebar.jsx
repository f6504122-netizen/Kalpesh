import React, { useState, useEffect } from "react";
import { 
  db, 
  auth, 
  collection, 
  onSnapshot, 
  query, 
  orderBy, 
  firebaseSignOut 
} from "../firebase";

export default function Sidebar({ currentUser, activeRecipient, onSelectRecipient }) {
  const [users, setUsers] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);

  // Real-time listener for KevSpace registered users in Firestore
  useEffect(() => {
    if (!currentUser) return;

    const usersCollectionRef = collection(db, "users");
    // Listen to all users ordered by displayName
    const q = query(usersCollectionRef, orderBy("displayName", "asc"));

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const fetchedUsers = [];
        snapshot.forEach((doc) => {
          const data = doc.data();
          // Exclude currently logged-in user from the contact selection list
          if (doc.id !== currentUser.uid) {
            fetchedUsers.push({ id: doc.id, ...data });
          }
        });
        setUsers(fetchedUsers);
        setLoading(false);
      },
      (error) => {
        console.error("Firestore Users onSnapshot error:", error);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [currentUser]);

  // Handle Sign Out
  const handleSignOut = async () => {
    try {
      await firebaseSignOut(auth);
    } catch (err) {
      console.error("Error signing out:", err);
    }
  };

  // Filter users in real-time based on search input
  const filteredUsers = users.filter((u) => {
    const nameMatch = u.displayName?.toLowerCase().includes(searchQuery.toLowerCase());
    const emailMatch = u.email?.toLowerCase().includes(searchQuery.toLowerCase());
    return nameMatch || emailMatch;
  });

  return (
    <aside className="w-full md:w-80 lg:w-96 h-full flex flex-col bg-slate-900 border-r border-slate-800 select-none">
      {/* 1. KevSpace Header Banner */}
      <div className="p-4 sm:p-5 bg-gradient-to-r from-indigo-950 via-slate-900 to-slate-950 border-b border-indigo-500/20 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-cyan-400 p-0.5 shadow-md shadow-indigo-500/20">
            <div className="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
              <svg className="w-5 h-5 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
          </div>
          <div>
            <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-1.5">
              <span>KevSpace</span>
              <span className="text-[10px] uppercase font-semibold tracking-wider px-1.5 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                Live
              </span>
            </h2>
            <p className="text-xs text-slate-400 font-medium">Real-Time Messaging</p>
          </div>
        </div>

        <button
          onClick={handleSignOut}
          title="Sign out of KevSpace"
          className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
        </button>
      </div>

      {/* 2. Logged-in User Profile Bar */}
      <div className="p-3.5 bg-slate-950/60 border-b border-slate-800/80 flex items-center gap-3">
        <div className="relative">
          <img
            src={currentUser.photoURL || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser.displayName || "User")}&background=6366F1&color=fff`}
            alt={currentUser.displayName || "User Avatar"}
            className="w-10 h-10 rounded-full object-cover ring-2 ring-indigo-500/50"
          />
          <span className="absolute bottom-0 right-0 w-3 h-3 bg-emerald-500 border-2 border-slate-950 rounded-full" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-slate-200 truncate">
            {currentUser.displayName || "Anonymous User"}
          </p>
          <p className="text-xs text-slate-400 truncate">
            {currentUser.email || "No email available"}
          </p>
        </div>
      </div>

      {/* 3. Search Bar */}
      <div className="p-3.5 border-b border-slate-800/60">
        <div className="relative flex items-center">
          <span className="absolute left-3 text-slate-400">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </span>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search users or conversations..."
            className="w-full bg-slate-950/80 text-sm text-slate-200 pl-9 pr-8 py-2 rounded-xl border border-slate-800 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 placeholder-slate-500 transition"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery("")}
              className="absolute right-2.5 text-slate-400 hover:text-slate-200 text-xs"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {/* 4. Real-time User / Chat List */}
      <div className="flex-1 overflow-y-auto divide-y divide-slate-800/30">
        <div className="px-4 py-2.5 bg-slate-950/40 text-[11px] font-bold uppercase tracking-wider text-slate-400 flex justify-between items-center">
          <span>Active Users ({filteredUsers.length})</span>
          <span className="flex items-center gap-1 text-emerald-400 font-normal">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Live Sync
          </span>
        </div>

        {loading ? (
          <div className="p-8 text-center text-slate-500 text-sm flex flex-col items-center gap-2">
            <div className="w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin" />
            <span>Loading KevSpace directory...</span>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            {searchQuery ? "No members match your search." : "No other users registered yet. Open another tab or invite a friend!"}
          </div>
        ) : (
          filteredUsers.map((user) => {
            const isSelected = activeRecipient?.id === user.id;

            return (
              <div
                key={user.id}
                onClick={() => onSelectRecipient(user)}
                className={`p-3.5 flex items-center gap-3 cursor-pointer transition duration-150 ${
                  isSelected
                    ? "bg-indigo-600/20 border-l-4 border-indigo-500"
                    : "hover:bg-slate-800/60"
                }`}
              >
                <div className="relative flex-shrink-0">
                  <img
                    src={user.photoURL || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.displayName || "User")}&background=6366F1&color=fff`}
                    alt={user.displayName}
                    className="w-11 h-11 rounded-full object-cover ring-1 ring-slate-700"
                  />
                  <span
                    className={`absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-slate-900 ${
                      user.status === "online" ? "bg-emerald-500" : "bg-slate-500"
                    }`}
                  />
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-0.5">
                    <p className={`text-sm font-semibold truncate ${isSelected ? "text-indigo-300" : "text-slate-100"}`}>
                      {user.displayName || "Unknown User"}
                    </p>
                    <span className="text-[11px] text-slate-500">
                      {user.status === "online" ? "Online" : "Away"}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 truncate">
                    {user.email || "Tap to start conversation"}
                  </p>
                </div>
              </div>
            );
          })
        )}
      </div>
    </aside>
  );
}
