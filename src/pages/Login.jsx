import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { signInWithPopup } from "firebase/auth";
import { auth, googleProvider, db } from "../firebase/firebase";
import { doc, setDoc } from "firebase/firestore";
import useParticles from "../hooks/useParticles";

export default function Login() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  useParticles();

  const handleGoogleLogin = async () => {
    if (loading) return;
    setLoading(true);

    try {
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;

      const userRef = doc(db, "users", user.uid);

      await setDoc(
        userRef,
        {
          email: user.email,
          displayName: user.displayName,
          photoURL: user.photoURL,
          createdAt: new Date().toISOString(),
        },
        { merge: true }
      );

      navigate("/app");
    } catch (err) {
      console.error("Google Sign-In Error:", err);
      alert("Login failed. Please try again.");
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="particles" id="particles"></div>

      <header className="header">
        <div className="logo pulse">⚡ ZapGo</div>
        <button
          className="admin-btn"
          onClick={() => navigate("/admin")}
        >
          Admin Login
        </button>
      </header>

      <div className="login-container relative z-10">
        <div className="text-center mb-8 relative z-10">
          <h1 className="title">Welcome to ZapGo</h1>
          <p className="text-gray-600 text-lg">
            Your journey to smarter EV travel starts here
          </p>
        </div>

        <div className="mb-6 relative z-10">
          <button
            className={`google-btn w-full ${loading ? "loading" : ""}`}
            onClick={handleGoogleLogin}
            disabled={loading}
          >
            <i className="bi bi-google"></i>
            <span>Continue with Google</span>
            <div className="spinner"></div>
          </button>
        </div>

        <div className="feature-card relative z-10">
          <div className="feature-item">
            <div className="feature-icon">
              <i className="bi bi-lightning-charge-fill"></i>
            </div>
            <span className="font-medium">Find optimal charging stations</span>
          </div>
          <div className="feature-item">
            <div className="feature-icon">
              <i className="bi bi-map-fill"></i>
            </div>
            <span className="font-medium">Smart route planning</span>
          </div>
          <div className="feature-item">
            <div className="feature-icon">
              <i className="bi bi-clock-fill"></i>
            </div>
            <span className="font-medium">Real-time availability updates</span>
          </div>
        </div>
      </div>
    </div>
  );
}
