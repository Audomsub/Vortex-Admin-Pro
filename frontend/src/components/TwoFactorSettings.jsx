import { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { ShieldCheck, ShieldOff, Copy, Check } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { twoFactorService } from '../services/twoFactorService';

/**
 * Two-factor authentication settings panel that allows users to enable or
 * disable TOTP-based 2FA. Displays a QR code and manual secret for setup,
 * shows one-time backup codes after activation, and prompts for a TOTP code
 * to confirm disabling 2FA.
 * @returns {JSX.Element}
 */
const TwoFactorSettings = () => {
    const { t } = useTranslation();
    const [status, setStatus] = useState({ enabled: false, remainingBackupCodes: 0 });
    const [setupData, setSetupData] = useState(null); // { secret, otpAuthUrl }
    const [code, setCode] = useState('');
    const [backupCodes, setBackupCodes] = useState(null);
    const [disableMode, setDisableMode] = useState(false);
    const [error, setError] = useState('');
    const [copied, setCopied] = useState(false);

    /**
     * Fetches the current 2FA enablement status from GET /2fa/status and
     * updates the local state.
     * @returns {Promise<void>}
     */
    async function fetchStatus() {
        try {
            const res = await twoFactorService.getStatus();
            setStatus(res.data.data);
        } catch (err) {
        }
    };

    useEffect(() => {
        fetchStatus();
    }, []);

    /**
     * Initiates the 2FA setup flow via POST /2fa/setup and stores the
     * returned secret and OTP auth URL for QR code rendering.
     * @returns {Promise<void>}
     */
    async function handleSetup() {
        setError('');
        try {
            const res = await twoFactorService.setup();
            setSetupData(res.data.data);
        } catch (err) {
            setError(err.response?.data?.message || t('common.error'));
        }
    };

    /**
     * Submits the entered TOTP code to POST /2fa/verify to activate 2FA and
     * reveals the one-time backup codes.
     * @param {React.FormEvent} e
     * @returns {Promise<void>}
     */
    async function handleVerify(e) {
        e.preventDefault();
        setError('');
        try {
            const res = await twoFactorService.verify(code);
            setBackupCodes(res.data.data.backupCodes || []);
            setSetupData(null);
            setCode('');
            fetchStatus();
        } catch (err) {
            setError(err.response?.data?.message || t('auth.invalidCode'));
        }
    };

    /**
     * Submits the current TOTP or backup code to POST /2fa/disable to turn off
     * two-factor authentication.
     * @param {React.FormEvent} e
     * @returns {Promise<void>}
     */
    async function handleDisable(e) {
        e.preventDefault();
        setError('');
        try {
            await twoFactorService.disable(code);
            setDisableMode(false);
            setCode('');
            setBackupCodes(null);
            fetchStatus();
        } catch (err) {
            setError(err.response?.data?.message || t('auth.invalidCode'));
        }
    };

    /**
     * Copies the TOTP secret to the system clipboard and briefly shows a
     * confirmation checkmark icon.
     */
    const copySecret = () => {
        navigator.clipboard.writeText(setupData.secret);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="bg-surface border border-border rounded-2xl p-6 space-y-4">
            <div className="flex items-center justify-between gap-4">
                <div>
                    <h3 className="text-lg font-semibold text-text-primary flex items-center gap-2">
                        {status.enabled
                            ? <ShieldCheck className="w-5 h-5 text-success" />
                            : <ShieldOff className="w-5 h-5 text-text-secondary" />}
                        {t('twoFactor.title')}
                    </h3>
                    <p className="text-sm text-text-secondary mt-1">{t('twoFactor.description')}</p>
                    {status.enabled && (
                        <p className="text-xs text-text-secondary mt-1">
                            {t('twoFactor.remainingBackupCodes', { count: status.remainingBackupCodes })}
                        </p>
                    )}
                </div>
                {!status.enabled && !setupData && (
                    <button
                        onClick={handleSetup}
                        className="shrink-0 px-4 py-2 bg-primary hover:bg-primary-hover text-white text-sm rounded-xl font-medium transition-all active:scale-95"
                    >
                        {t('twoFactor.enable')}
                    </button>
                )}
                {status.enabled && !disableMode && (
                    <button
                        onClick={() => { setDisableMode(true); setError(''); setCode(''); }}
                        className="shrink-0 px-4 py-2 bg-danger/10 hover:bg-danger/20 text-danger text-sm rounded-xl font-medium transition-all active:scale-95"
                    >
                        {t('twoFactor.disable')}
                    </button>
                )}
            </div>

            {error && (
                <div className="bg-danger/10 border border-danger/20 text-danger text-sm rounded-xl p-3">
                    {error}
                </div>
            )}

            {/* Setup: QR + verification */}
            {setupData && (
                <div className="border-t border-border pt-4 space-y-4">
                    <p className="text-sm font-medium text-text-primary">{t('twoFactor.scanQr')}</p>
                    <div className="flex flex-col sm:flex-row items-center gap-6">
                        <div className="bg-white p-3 rounded-2xl">
                            <QRCodeSVG value={setupData.otpAuthUrl} size={160} />
                        </div>
                        <div className="flex-1 space-y-3 w-full">
                            <div>
                                <p className="text-xs text-text-secondary mb-1">{t('twoFactor.manualEntry')}</p>
                                <div className="flex items-center gap-2">
                                    <code className="flex-1 text-xs bg-black/5 dark:bg-white/5 rounded-lg px-3 py-2 break-all text-text-primary">
                                        {setupData.secret}
                                    </code>
                                    <button
                                        onClick={copySecret}
                                        className="p-2 text-text-secondary hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
                                    >
                                        {copied ? <Check className="w-4 h-4 text-success" /> : <Copy className="w-4 h-4" />}
                                    </button>
                                </div>
                            </div>
                            <form onSubmit={handleVerify} className="space-y-2">
                                <p className="text-xs text-text-secondary">{t('twoFactor.enterCode')}</p>
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        inputMode="numeric"
                                        required
                                        maxLength={6}
                                        value={code}
                                        onChange={(e) => setCode(e.target.value.replace(/[^0-9]/g, ''))}
                                        className="flex-1 px-4 py-2.5 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary text-center tracking-[0.4em] outline-none transition-all"
                                        placeholder="000000"
                                    />
                                    <button
                                        type="submit"
                                        className="px-4 py-2.5 bg-primary hover:bg-primary-hover text-white text-sm rounded-xl font-medium transition-all active:scale-95"
                                    >
                                        {t('auth.verify')}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {/* Backup codes shown once after enabling */}
            {backupCodes && backupCodes.length > 0 && (
                <div className="border-t border-border pt-4 space-y-3">
                    <p className="text-sm font-semibold text-text-primary">{t('twoFactor.backupCodes')}</p>
                    <p className="text-xs text-text-secondary">{t('twoFactor.backupCodesHint')}</p>
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                        {backupCodes.map((bc) => (
                            <code key={bc} className="text-sm text-center bg-black/5 dark:bg-white/5 rounded-lg px-2 py-2 text-text-primary font-mono">
                                {bc}
                            </code>
                        ))}
                    </div>
                </div>
            )}

            {/* Disable confirmation */}
            {disableMode && (
                <form onSubmit={handleDisable} className="border-t border-border pt-4 space-y-2">
                    <p className="text-sm text-text-secondary">{t('twoFactor.disablePrompt')}</p>
                    <div className="flex gap-2">
                        <input
                            type="text"
                            inputMode="numeric"
                            required
                            maxLength={8}
                            value={code}
                            onChange={(e) => setCode(e.target.value.replace(/[^0-9]/g, ''))}
                            className="flex-1 px-4 py-2.5 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary text-center tracking-[0.4em] outline-none transition-all"
                            placeholder="000000"
                        />
                        <button
                            type="submit"
                            className="px-4 py-2.5 bg-danger hover:bg-danger/90 text-white text-sm rounded-xl font-medium transition-all active:scale-95"
                        >
                            {t('twoFactor.disable')}
                        </button>
                        <button
                            type="button"
                            onClick={() => { setDisableMode(false); setCode(''); setError(''); }}
                            className="px-4 py-2.5 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 text-sm rounded-xl font-medium transition-colors"
                        >
                            {t('common.cancel')}
                        </button>
                    </div>
                </form>
            )}
        </div>
    );
};

export default TwoFactorSettings;
