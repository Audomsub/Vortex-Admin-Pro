import { useState } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Book, Server, Shield, ChevronRight, Terminal, Copy, Check
} from 'lucide-react';
import { cn } from '../lib/utils';
import { useTranslation } from 'react-i18next';

const Docs = () => {
    const { t } = useTranslation();
    const [activeSection, setActiveSection] = useState('intro');
    const [copiedContent, setCopiedContent] = useState(null);

    const DOCS_MENU = [
        {
            category: t('docs.categoryGettingStarted'),
            items: [
                { id: 'intro', title: t('docs.itemIntro') },
                { id: 'auth', title: t('docs.itemAuth') },
                { id: 'errors', title: t('docs.itemErrors') },
            ]
        },
        {
            category: t('docs.categoryApiEndpoints'),
            items: [
                { id: 'users', title: t('docs.itemUsers') },
                { id: 'teams', title: t('docs.itemTeams') },
                { id: 'tasks', title: t('docs.itemTasks') },
            ]
        }
    ];

    const handleCopy = (text) => {
        navigator.clipboard.writeText(text);
        setCopiedContent(text);
        setTimeout(() => setCopiedContent(null), 2000);
    };

    const CodeBlock = ({ language, code }) => (
        <div className="relative group bg-background dark:bg-black rounded-xl overflow-hidden my-4 border border-border/50">
            <div className="flex items-center justify-between px-4 py-2 bg-surface dark:bg-surface border-b border-border/50">
                <span className="text-xs font-mono text-text-secondary">{language}</span>
                <button 
                    onClick={() => handleCopy(code)}
                    className="p-1 text-text-secondary hover:text-text-primary transition-colors"
                >
                    {copiedContent === code ? <Check className="w-4 h-4 text-success" /> : <Copy className="w-4 h-4" />}
                </button>
            </div>
            <pre className="p-4 overflow-x-auto text-sm text-zinc-300 font-mono">
                <code>{code}</code>
            </pre>
        </div>
    );

    const renderContent = () => {
        switch (activeSection) {
            case 'intro':
                return (
                    <div className="space-y-6">
                        <div>
                            <h1 className="text-3xl font-bold text-text-primary mb-2">{t('docs.introTitle')}</h1>
                            <p className="text-text-secondary text-lg">{t('docs.introSubtitle')}</p>
                        </div>
                        
                        <div className="prose dark:prose-invert max-w-none text-text-secondary">
                            <p>
                                {t('docs.introBody')}
                            </p>
                            
                            <h3 className="text-xl font-bold text-text-primary mt-8 mb-4">{t('docs.baseUrl')}</h3>
                            <div className="bg-black/5 dark:bg-white/5 border border-border rounded-xl p-4 flex items-center gap-3">
                                <Server className="w-5 h-5 text-primary" />
                                <code className="text-sm font-mono text-text-primary">https://api.vortexadmin.com/v1</code>
                            </div>
                        </div>
                    </div>
                );
            case 'auth':
                return (
                    <div className="space-y-6">
                        <h1 className="text-3xl font-bold text-text-primary mb-2">{t('docs.authTitle')}</h1>
                        <p className="text-text-secondary">{t('docs.authSubtitle')}</p>

                        <div className="bg-blue-500/10 border border-blue-500/20 rounded-2xl p-6">
                            <div className="flex gap-3">
                                <Shield className="w-6 h-6 text-blue-500 shrink-0" />
                                <div>
                                    <h3 className="font-bold text-blue-500 mb-2">{t('docs.authBearerTitle')}</h3>
                                    <p className="text-sm text-blue-500/80 leading-relaxed">
                                        {t('docs.authBearerBody')} <a href="/api-keys" className="underline font-bold">{t('docs.authApiKeysLink')}</a> {t('docs.authBearerBodySuffix')}
                                    </p>
                                </div>
                            </div>
                        </div>

                        <h3 className="text-xl font-bold text-text-primary mt-8">{t('docs.exampleRequest')}</h3>
                        <CodeBlock 
                            language="bash" 
                            code={`curl -X GET https://api.vortexadmin.com/v1/users \\
  -H "Authorization: Bearer vrx_live_xxxxxxxxxxxxxxxxx"`} 
                        />
                    </div>
                );
            case 'users':
                return (
                    <div className="space-y-6">
                        <h1 className="text-3xl font-bold text-text-primary mb-2">{t('docs.usersApiTitle')}</h1>
                        <p className="text-text-secondary">{t('docs.usersApiSubtitle')}</p>

                        <div className="border border-border rounded-2xl overflow-hidden mt-8">
                            <div className="bg-black/5 dark:bg-white/5 px-6 py-4 border-b border-border flex items-center gap-4">
                                <span className="px-2 py-1 bg-success/10 text-success text-xs font-bold rounded">GET</span>
                                <code className="text-sm font-mono text-text-primary">/users</code>
                            </div>
                            <div className="p-6">
                                <p className="text-text-secondary mb-4">{t('docs.usersApiDescription')}</p>
                                
                                <h4 className="font-bold text-sm text-text-primary mb-2 uppercase tracking-wider">{t('docs.responseExample')}</h4>
                                <CodeBlock 
                                    language="json" 
                                    code={`{
  "status": "success",
  "data": [
    {
      "id": 1,
      "email": "user@vortex.com",
      "firstName": "John",
      "lastName": "Doe",
      "status": "ACTIVE"
    }
  ],
  "meta": {
    "total": 45,
    "page": 1
  }
}`} 
                                />
                            </div>
                        </div>
                    </div>
                );
            default:
                return (
                    <div className="py-20 flex flex-col items-center text-center text-text-secondary">
                        <Terminal className="w-16 h-16 opacity-20 mb-4" />
                        <h2 className="text-xl font-bold text-text-primary mb-2">{t('docs.comingSoon')}</h2>
                        <p>{t('docs.comingSoonBody')}</p>
                    </div>
                );
        }
    };

    return (
        <Layout>
            <div className="flex flex-col lg:flex-row min-h-[calc(100vh-theme(spacing.16))] h-full">
                {/* Sidebar Navigation */}
                <div className="w-full lg:w-64 shrink-0 border-r border-border bg-surface lg:sticky lg:top-0 lg:h-[calc(100vh-theme(spacing.16))] overflow-y-auto p-4 lg:p-6 hide-scrollbar">
                    <div className="flex items-center gap-2 mb-8 px-2">
                        <Book className="w-5 h-5 text-primary" />
                        <span className="font-bold text-text-primary">{t('docs.sidebarTitle')}</span>
                    </div>

                    <div className="space-y-6">
                        {DOCS_MENU.map((section, idx) => (
                            <div key={idx}>
                                <h4 className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-3 px-2">
                                    {section.category}
                                </h4>
                                <ul className="space-y-1">
                                    {section.items.map(item => (
                                        <li key={item.id}>
                                            <button
                                                onClick={() => setActiveSection(item.id)}
                                                className={cn(
                                                    "w-full flex items-center justify-between px-3 py-2 rounded-lg text-sm transition-colors",
                                                    activeSection === item.id
                                                        ? "bg-primary/10 text-primary font-medium"
                                                        : "text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 hover:text-text-primary"
                                                )}
                                            >
                                                {item.title}
                                                {activeSection === item.id && <ChevronRight className="w-4 h-4" />}
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Main Content Area */}
                <div className="flex-1 p-4 lg:p-12 overflow-y-auto bg-background">
                    <div className="max-w-3xl">
                        {renderContent()}
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default Docs;
