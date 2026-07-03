import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Mail, Save, Code, Eye, AlertCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';
import { toast } from '../components/ui/toastHelper';

const defaultTemplate = `<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f4f5; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 24px; }
        .btn { display: inline-block; padding: 12px 24px; background-color: #4f46e5; color: white; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 16px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Welcome to Vortex!</h2>
        </div>
        <p>Hi {{name}},</p>
        <p>Thank you for joining us. We're excited to have you on board!</p>
        <div style="text-align: center;">
            <a href="{{login_url}}" class="btn">Login Now</a>
        </div>
    </div>
</body>
</html>`;

const EmailBuilder = () => {
    const { t } = useTranslation();
    const [selectedTemplate, setSelectedTemplate] = useState('welcome');
    const [subject, setSubject] = useState('Welcome to Vortex Admin Pro!');
    const [code, setCode] = useState(defaultTemplate);
    const [activeTab, setActiveTab] = useState('split');
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        api.get('/email-templates').then(res => {
            const welcome = (res.data.data || []).find(tmpl => tmpl.name === 'welcome');
            if (welcome) {
                setCode(welcome.content);
                setSubject(welcome.subject);
            }
        }).catch(err => console.error(err));
    }, []);

    const handleTemplateChange = async (name) => {
        setSelectedTemplate(name);
        try {
            const res = await api.get(`/email-templates/${name}`);
            if (res.data.data) {
                setCode(res.data.data.content);
                setSubject(res.data.data.subject);
            }
        } catch {
            setCode(defaultTemplate);
            setSubject('');
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            await api.post('/email-templates', {
                name: selectedTemplate,
                subject,
                content: code
            });
            toast.success(t('common.success'), t('emailBuilder.saveSuccess'));
        } catch (error) {
            console.error('Save failed', error);
            toast.error(t('common.error'), error.response?.data?.message || t('emailBuilder.saveFailed'));
        } finally {
            setSaving(false);
        }
    };

    return (
        <Layout>
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 flex flex-col h-[calc(100vh-100px)]">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 shrink-0">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('emailBuilder.title')}</h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('emailBuilder.subtitle')}</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <select
                            value={selectedTemplate}
                            onChange={(e) => handleTemplateChange(e.target.value)}
                            className="bg-surface border border-border text-text-primary text-sm rounded-xl px-4 py-2 outline-none focus:border-primary"
                        >
                            <option value="welcome">Welcome Email</option>
                            <option value="reset_password">Reset Password</option>
                            <option value="invoice">Invoice Receipt</option>
                        </select>
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl font-medium transition-colors text-sm shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/30 disabled:opacity-50"
                        >
                            <Save size={16} /> {saving ? t('emailBuilder.saving') : t('emailBuilder.saveTemplate')}
                        </button>
                    </div>
                </div>

                <div className="bg-surface p-4 rounded-t-2xl border-x border-t border-border flex items-center justify-between shrink-0">
                    <div className="flex-1 max-w-xl flex items-center gap-3">
                        <span className="text-sm font-medium text-text-secondary">{t('emailBuilder.subjectLabel')}</span>
                        <input
                            type="text"
                            value={subject}
                            onChange={(e) => setSubject(e.target.value)}
                            className="flex-1 bg-background border border-border rounded-lg px-3 py-1.5 text-sm outline-none focus:border-primary text-text-primary"
                            placeholder={t('emailBuilder.subjectPlaceholder')}
                        />
                    </div>
                    <div className="flex p-1 bg-background rounded-lg border border-border ml-4">
                        <button
                            onClick={() => setActiveTab('code')}
                            className={`px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1.5 transition-colors ${activeTab === 'code' ? 'bg-surface shadow-sm text-text-primary' : 'text-text-secondary hover:text-text-primary'}`}
                        >
                            <Code size={14} /> Code
                        </button>
                        <button
                            onClick={() => setActiveTab('split')}
                            className={`px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1.5 transition-colors ${activeTab === 'split' ? 'bg-surface shadow-sm text-text-primary' : 'text-text-secondary hover:text-text-primary'}`}
                        >
                            <Mail size={14} /> Split View
                        </button>
                        <button
                            onClick={() => setActiveTab('preview')}
                            className={`px-3 py-1.5 text-xs font-medium rounded-md flex items-center gap-1.5 transition-colors ${activeTab === 'preview' ? 'bg-surface shadow-sm text-text-primary' : 'text-text-secondary hover:text-text-primary'}`}
                        >
                            <Eye size={14} /> Preview
                        </button>
                    </div>
                </div>

                <div className={`flex-1 min-h-0 bg-background border border-border rounded-b-2xl overflow-hidden flex ${activeTab === 'split' ? 'flex-row' : 'flex-col'}`}>
                    {(activeTab === 'code' || activeTab === 'split') && (
                        <div className={`flex flex-col border-r border-border ${activeTab === 'split' ? 'w-1/2' : 'w-full'}`}>
                            <div className="bg-surface px-4 py-2 text-xs font-medium text-text-secondary border-b border-border flex justify-between items-center">
                                <span>HTML Source</span>
                                <span className="flex items-center gap-1"><AlertCircle size={12} /> Variables: {'{{name}}'}, {'{{login_url}}'}</span>
                            </div>
                            <textarea
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                className="flex-1 w-full p-4 bg-[#1e1e1e] text-[#d4d4d4] font-mono text-sm outline-none resize-none"
                                spellCheck="false"
                            />
                        </div>
                    )}

                    {(activeTab === 'preview' || activeTab === 'split') && (
                        <div className={`flex flex-col bg-zinc-100 ${activeTab === 'split' ? 'w-1/2' : 'w-full'}`}>
                            <div className="bg-surface px-4 py-2 text-xs font-medium text-text-secondary border-b border-border">
                                Live Preview
                            </div>
                            <div className="flex-1 w-full h-full overflow-auto bg-zinc-100 p-4">
                                <div className="w-full max-w-2xl mx-auto shadow-sm rounded-lg overflow-hidden bg-white">
                                    <iframe
                                        title="Email Preview"
                                        srcDoc={code.replace('{{name}}', 'John Doe').replace('{{login_url}}', 'https://vortexadmin.com/login')}
                                        className="w-full h-[600px] border-0"
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </Layout>
    );
};

export default EmailBuilder;
