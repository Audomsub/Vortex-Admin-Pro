import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Sparkles, ArrowRight, Loader2, Bot } from 'lucide-react';
import { cn } from '../lib/utils';
import { useTranslation } from 'react-i18next';

const Welcome = () => {
    const { t } = useTranslation();
    const phrases = t('welcome.phrases', { returnObjects: true });
    const insights = t('welcome.insights', { returnObjects: true });

    const chatBubbles = [
        { text: t('welcome.chat1'), align: "right", color: "bg-surface text-text-primary border border-border shadow-sm" },
        { text: t('welcome.chat2'), align: "left", color: "bg-primary text-white shadow-sm shadow-primary/20" },
        { text: t('welcome.chat3'), align: "right", color: "bg-surface text-text-primary border border-border shadow-sm" },
        { text: t('welcome.chat4'), align: "left", color: "bg-accent text-slate-900 shadow-sm shadow-accent/20" },
    ];

    const [currentPhraseIndex, setCurrentPhraseIndex] = useState(0);
    const [fade, setFade] = useState(true);
    
    // AI Interaction state
    const [query, setQuery] = useState('');
    const [isAnalyzing, setIsAnalyzing] = useState(false);
    const [insight, setInsight] = useState('');
    const [showRightBubbles, setShowRightBubbles] = useState(true);

    useEffect(() => {
        if (!phrases || !phrases.length) return;
        
        const interval = setInterval(() => {
            setFade(false);
            setTimeout(() => {
                setCurrentPhraseIndex((prev) => (prev + 1) % phrases.length);
                setFade(true);
            }, 500);
        }, 5000);

        return () => clearInterval(interval);
    }, [phrases]);

    // Toggle chat bubbles by color (alignment) every 5 seconds
    useEffect(() => {
        const interval = setInterval(() => {
            setShowRightBubbles(prev => !prev);
        }, 5000);

        return () => clearInterval(interval);
    }, []);

    const handleAnalyze = () => {
        if (!query.trim()) return;
        setIsAnalyzing(true);
        setInsight('');
        
        // Simulate AI analysis delay
        setTimeout(() => {
            setIsAnalyzing(false);
            if (insights && insights.length) {
                setInsight(insights[Math.floor(Math.random() * insights.length)]);
            } else {
                setInsight("Analysis complete.");
            }
        }, 1500);
    };

    return (
        <Layout>
            <div className="relative w-full h-[calc(100vh-8rem)] min-h-[600px] rounded-3xl overflow-hidden bg-surface border border-border shadow-premium flex items-center p-8 lg:p-16 transition-colors duration-300 animate-enter stagger-1">
                
                {/* Unified Background with Theme Variables */}
                <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <div className="absolute top-[-10%] right-[-5%] w-[600px] h-[600px] rounded-full bg-primary/10 blur-[100px]"></div>
                    <div className="absolute bottom-[-20%] left-[-10%] w-[500px] h-[500px] rounded-full bg-secondary/10 blur-[100px]"></div>
                    <div className="absolute top-[20%] right-[20%] w-[300px] h-[300px] rounded-full bg-accent/10 blur-[80px]"></div>
                    
                    {/* Simulated 3D tracks */}
                    <div className="absolute top-[30%] right-[10%] w-[400px] h-[400px] border-[20px] border-text-primary/5 rounded-full blur-[2px] transform rotate-45"></div>
                    <div className="absolute top-[10%] right-[30%] w-[200px] h-[200px] border-[15px] border-primary/10 rounded-full blur-[1px]"></div>
                </div>

                <div className="relative z-10 grid grid-cols-1 lg:grid-cols-2 gap-12 w-full h-full items-center">
                    
                    {/* Left Column - Typography & CTA */}
                    <div className="flex flex-col justify-center space-y-8 max-w-2xl animate-enter stagger-2">
                        <div className="min-h-[160px] md:min-h-[200px] flex items-center">
                            <h1 className={cn(
                                "text-4xl md:text-5xl lg:text-6xl font-extrabold text-text-primary leading-[1.1] tracking-tight transition-opacity duration-500",
                                fade ? "opacity-100" : "opacity-0"
                            )}>
                                {phrases && phrases[currentPhraseIndex]}
                            </h1>
                        </div>
                        
                        <p className="text-lg text-text-secondary leading-relaxed max-w-xl animate-enter stagger-3">
                            {t('welcome.description')}
                        </p>

                        <div className="w-full max-w-md mt-4 relative space-y-4 animate-enter stagger-4">
                            <div className="flex items-center gap-3">
                                <div className="relative flex-1 group">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <Sparkles className={cn("w-5 h-5 transition-colors", isAnalyzing ? "text-primary animate-pulse" : "text-text-secondary/50 group-focus-within:text-primary")} />
                                    </div>
                                    <input 
                                        type="text" 
                                        value={query}
                                        onChange={(e) => setQuery(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleAnalyze()}
                                        placeholder={t('welcome.placeholder')} 
                                        className="w-full bg-transparent border border-border text-text-primary placeholder:text-text-secondary/50 rounded-xl py-3.5 pl-12 pr-4 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary/50 backdrop-blur-md transition-all shadow-inner"
                                        disabled={isAnalyzing}
                                    />
                                </div>
                                <button 
                                    onClick={handleAnalyze}
                                    disabled={!query.trim() || isAnalyzing}
                                    className="bg-primary hover:bg-primary-hover disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3.5 rounded-xl font-medium flex items-center gap-2 transition-all shadow-lg shadow-primary/25 hover:-translate-y-0.5 whitespace-nowrap"
                                >
                                    {isAnalyzing ? (
                                        <><Loader2 className="w-4 h-4 animate-spin" /> {t('welcome.btnAnalyzing')}</>
                                    ) : (
                                        <>{t('welcome.btnAnalyze')} <ArrowRight className="w-4 h-4" /></>
                                    )}
                                </button>
                            </div>
                            
                            {/* AI Insight Result */}
                            {insight && (
                                <div className="p-4 rounded-xl bg-primary/10 border border-primary/20 text-sm animate-fade-in flex items-start gap-3 backdrop-blur-sm">
                                    <Bot className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                                    <p className="text-text-primary leading-relaxed">{insight}</p>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Right Column - Chat Bubbles & Visuals */}
                    <div className="hidden lg:flex relative h-full w-full items-center justify-center animate-enter stagger-3">
                        <div className="relative w-full max-w-md h-[500px]">
                            {chatBubbles.map((bubble, index) => {
                                const isRight = bubble.align === 'right';
                                const isVisible = isRight === showRightBubbles;
                                
                                return (
                                    <div 
                                        key={index}
                                        className={cn(
                                            "absolute max-w-[280px] p-4 rounded-2xl shadow-xl transition-all duration-700 backdrop-blur-md transform",
                                            bubble.color,
                                            isVisible ? "opacity-100 translate-y-0 scale-100" : "opacity-0 translate-y-8 scale-95",
                                            isRight ? "rounded-tr-sm" : "rounded-tl-sm"
                                        )}
                                        style={{
                                            top: `${15 + (index * 20)}%`,
                                            [isRight ? 'left' : 'right']: `${10 + (index % 2 === 0 ? 0 : 20)}%`,
                                            zIndex: 10 + index,
                                            transitionDelay: isVisible ? `${(index > 1 ? 400 : 0)}ms` : '0ms'
                                        }}
                                    >
                                        <p className="text-sm font-medium leading-relaxed shadow-sm">{bubble.text}</p>
                                        {/* Tail */}
                                        <div className={cn(
                                            "absolute top-0 w-4 h-4 transform rotate-45 -z-10",
                                            bubble.color.split(' ').find(c => c.startsWith('bg-')), // extract just the background color class
                                            isRight ? "-right-1" : "-left-1"
                                        )}></div>
                                    </div>
                                );
                            })}

                            {/* Floating decorative spheres */}
                            <div className="absolute top-[20%] right-[10%] w-16 h-16 rounded-full bg-gradient-to-br from-warning to-danger shadow-[0_0_30px_rgba(245,158,11,0.5)] animate-bounce" style={{ animationDuration: '3s' }}></div>
                            <div className="absolute bottom-[30%] left-[20%] w-12 h-12 rounded-full bg-gradient-to-br from-primary to-accent shadow-[0_0_30px_rgba(99,102,241,0.5)] animate-pulse" style={{ animationDuration: '4s' }}></div>
                            <div className="absolute top-[50%] right-[40%] w-8 h-8 rounded-full bg-gradient-to-br from-success to-emerald-400 shadow-[0_0_20px_rgba(16,185,129,0.5)] animate-bounce" style={{ animationDuration: '2.5s' }}></div>
                        </div>
                    </div>

                </div>
            </div>
        </Layout>
    );
};

export default Welcome;
