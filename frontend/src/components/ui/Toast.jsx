import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { X, CheckCircle, AlertTriangle, AlertCircle, Info, ExternalLink } from 'lucide-react';
import { cn } from '../../lib/utils';

// AudioContext singleton — created only after first user gesture to avoid browser warning
let _audioCtx = null;
let _audioReady = false;

const unlockAudio = () => {
    if (_audioReady) return;
    try {
        const AC = window.AudioContext || window.webkitAudioContext;
        if (!AC) return;
        _audioCtx = new AC();
        _audioReady = true;
    } catch { /* ignore */ }
};

['click', 'keydown', 'touchstart'].forEach(evt =>
    window.addEventListener(evt, unlockAudio, { once: true, capture: true })
);

const playChime = () => {
    if (!_audioReady || !_audioCtx) return;
    try {
        const playTone = (freq, time, duration) => {
            const osc = _audioCtx.createOscillator();
            const gain = _audioCtx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(freq, time);
            gain.gain.setValueAtTime(0, time);
            gain.gain.linearRampToValueAtTime(0.12, time + 0.03);
            gain.gain.exponentialRampToValueAtTime(0.001, time + duration);
            osc.connect(gain);
            gain.connect(_audioCtx.destination);
            osc.start(time);
            osc.stop(time + duration);
        };
        const now = _audioCtx.currentTime;
        playTone(523.25, now, 0.25);       // C5
        playTone(659.25, now + 0.08, 0.35); // E5
    } catch { /* ignore */ }
};

// Single Toast Card Component
const ToastCard = ({ item, onRemove }) => {
    const navigate = useNavigate();
    const [progress, setProgress] = useState(100);
    const [isExiting, setIsExiting] = useState(false);
    const progressInterval = useRef(null);
    const startTime = useRef(0);
    const remainingTime = useRef(item.duration);
    const isHovered = useRef(false);

    const handleClose = () => {
        setIsExiting(true);
        setTimeout(() => {
            onRemove(item.id);
        }, 300); // match animation exit time
    };

    const handleActionClick = (e) => {
        e.stopPropagation();
        if (item.action) {
            if (item.action.path) {
                navigate(item.action.path);
            }
            if (typeof item.action.onClick === 'function') {
                item.action.onClick();
            }
        }
        handleClose();
    };

    useEffect(() => {
        const totalDuration = item.duration;
        const step = 50;

        const updateProgress = () => {
            if (isHovered.current) {
                // Pause timer on hover
                startTime.current = Date.now() - (totalDuration - remainingTime.current);
                return;
            }

            const elapsed = Date.now() - startTime.current;
            const remaining = Math.max(0, totalDuration - elapsed);
            remainingTime.current = remaining;
            
            const percentage = (remaining / totalDuration) * 100;
            setProgress(percentage);

            if (remaining <= 0) {
                clearInterval(progressInterval.current);
                handleClose();
            }
        };

        startTime.current = Date.now();
        progressInterval.current = setInterval(updateProgress, step);

        return () => {
            if (progressInterval.current) clearInterval(progressInterval.current);
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [item]);

    // Icon & styles map
    const config = {
        success: {
            icon: <CheckCircle className="w-5 h-5 text-success shrink-0" />,
            border: 'border-success/30 hover:border-success/60',
            glow: 'rgba(16, 185, 129, 0.1)',
            barBg: 'bg-success'
        },
        error: {
            icon: <AlertCircle className="w-5 h-5 text-danger shrink-0" />,
            border: 'border-danger/30 hover:border-danger/60',
            glow: 'rgba(244, 63, 94, 0.1)',
            barBg: 'bg-danger'
        },
        warning: {
            icon: <AlertTriangle className="w-5 h-5 text-warning shrink-0" />,
            border: 'border-warning/30 hover:border-warning/60',
            glow: 'rgba(245, 158, 11, 0.1)',
            barBg: 'bg-warning'
        },
        info: {
            icon: <Info className="w-5 h-5 text-primary shrink-0" />,
            border: 'border-primary/30 hover:border-primary/60',
            glow: 'rgba(99, 102, 241, 0.1)',
            barBg: 'bg-primary'
        }
    };

    const currentConfig = config[item.type] || config.info;

    return (
        <div
            className={cn(
                "relative w-full max-w-sm overflow-hidden rounded-2xl border bg-surface/80 p-4 shadow-xl glass transition-all duration-300 hover:shadow-2xl flex flex-col gap-2 pointer-events-auto",
                isExiting ? "toast-exit" : "toast-enter",
                currentConfig.border
            )}
            style={{
                boxShadow: `0 8px 30px -10px ${currentConfig.glow}`
            }}
            onMouseEnter={() => { isHovered.current = true; }}
            onMouseLeave={() => { isHovered.current = false; }}
        >
            <div className="flex gap-3">
                {currentConfig.icon}
                <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-semibold text-text-primary leading-tight truncate">{item.title}</h4>
                    <p className="text-xs text-text-secondary mt-1 leading-relaxed break-words">{item.message}</p>
                </div>
                <button
                    onClick={handleClose}
                    className="p-1 hover:bg-black/5 dark:hover:bg-white/5 rounded-lg text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
                >
                    <X className="w-4 h-4" />
                </button>
            </div>

            {/* Action button if provided */}
            {item.action && (
                <div className="flex justify-end mt-1 pl-8">
                    <button
                        onClick={handleActionClick}
                        className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-gradient-to-r from-primary to-secondary rounded-lg hover:shadow-md hover:shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all cursor-pointer"
                    >
                        <span>{item.action.label || 'Go'}</span>
                        <ExternalLink className="w-3.5 h-3.5" />
                    </button>
                </div>
            )}

            {/* Micro Timer Bar */}
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-black/5 dark:bg-white/5">
                <div
                    className={cn("h-full transition-all duration-75", currentConfig.barBg)}
                    style={{ width: `${progress}%` }}
                ></div>
            </div>
        </div>
    );
};

// Global Toast Container Component
export const ToastContainer = () => {
    const [toasts, setToasts] = useState([]);

    useEffect(() => {
        const handleNewToast = (e) => {
            const newToast = e.detail;
            setToasts((prev) => [...prev, newToast]);
            playChime();
        };

        window.addEventListener('vortex-toast', handleNewToast);
        return () => window.removeEventListener('vortex-toast', handleNewToast);
    }, []);

    const removeToast = (id) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    };

    return (
        <div className="fixed bottom-5 right-5 z-[9999] flex flex-col gap-3 w-[360px] max-w-[calc(100vw-40px)] pointer-events-none">
            {toasts.map((item) => (
                <ToastCard key={item.id} item={item} onRemove={removeToast} />
            ))}
        </div>
    );
};
