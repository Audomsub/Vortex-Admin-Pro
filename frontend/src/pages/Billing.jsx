import { useState, useEffect, useCallback } from 'react';
import Layout from '../components/layout/Layout';
import {
    CreditCard, Check, Users, HardDrive, Building2, Receipt, XCircle
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { cn } from '../lib/utils';
import { billingService } from '../services/billingService';
import { useOrganizationStore } from '../store/organizationStore';

const PLAN_ORDER = ['FREE', 'PRO', 'BUSINESS', 'ENTERPRISE'];

const Billing = () => {
    const { t } = useTranslation();
    const { organizations, currentOrgId, fetchOrganizations } = useOrganizationStore();
    const [orgId, setOrgId] = useState(currentOrgId);
    const [plans, setPlans] = useState([]);
    const [subscription, setSubscription] = useState(null);
    const [invoices, setInvoices] = useState([]);
    const [billingCycle, setBillingCycle] = useState('MONTHLY');
    const [loading, setLoading] = useState(true);
    const [working, setWorking] = useState(false);
    const [confirmModal, setConfirmModal] = useState({ isOpen: false, planName: null });

    useEffect(() => {
        fetchOrganizations();
        billingService.getPlans()
            .then(res => setPlans((res.data.data || []).sort(
                (a, b) => PLAN_ORDER.indexOf(a.name) - PLAN_ORDER.indexOf(b.name))))
            .catch(err => console.error('Failed to fetch plans', err));
    }, [fetchOrganizations]);

    useEffect(() => {
        if (!orgId && currentOrgId) setOrgId(currentOrgId);
    }, [currentOrgId, orgId]);

    const loadBilling = useCallback(async () => {
        if (!orgId) {
            setLoading(false);
            return;
        }
        setLoading(true);
        try {
            const [subRes, invRes] = await Promise.all([
                billingService.getSubscription(orgId),
                billingService.getInvoices(orgId),
            ]);
            setSubscription(subRes.data.data);
            setInvoices(invRes.data.data || []);
        } catch (error) {
            console.error('Failed to load billing:', error);
            setSubscription(null);
            setInvoices([]);
        } finally {
            setLoading(false);
        }
    }, [orgId]);

    useEffect(() => {
        loadBilling();
    }, [loadBilling]);

    const handleUpgradeClick = (planName) => {
        setConfirmModal({ isOpen: true, planName });
    };

    async function confirmUpgrade() {
        if (!confirmModal.planName) return;
        setWorking(true);
        try {
            await billingService.upgrade(orgId, confirmModal.planName, billingCycle);
            await loadBilling();
            fetchOrganizations();
            setConfirmModal({ isOpen: false, planName: null });
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        } finally {
            setWorking(false);
        }
    };

    async function handleCancel() {
        if (!window.confirm(t('billing.cancelSubscription') + '?')) return;
        setWorking(true);
        try {
            await billingService.cancel(orgId);
            await loadBilling();
            fetchOrganizations();
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        } finally {
            setWorking(false);
        }
    };

    const usagePercent = (used, max) => {
        if (!max || max <= 0) return 0;
        return Math.min(100, Math.round((used / max) * 100));
    };

    const currentPlanName = subscription?.plan?.name;

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <CreditCard className="w-6 h-6 text-primary" />
                            {t('billing.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('billing.selectOrganization')}</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <Building2 className="w-4 h-4 text-text-secondary" />
                        <select
                            value={orgId || ''}
                            onChange={(e) => setOrgId(Number(e.target.value))}
                            className="px-4 py-2.5 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all text-text-primary"
                        >
                            <option value="" disabled>{t('billing.selectOrganization')}</option>
                            {organizations.map((org) => (
                                <option key={org.id} value={org.id}>{org.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : !subscription ? (
                    <div className="py-12 text-center text-text-secondary bg-surface border border-dashed border-border rounded-2xl">
                        <CreditCard className="w-12 h-12 mx-auto mb-3 opacity-20" />
                        <p>{t('billing.selectOrganization')}</p>
                    </div>
                ) : (
                    <>
                        {/* Current Plan + Usage */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                            <div className="bg-surface border border-border rounded-2xl p-6">
                                <p className="text-sm text-text-secondary mb-1">{t('billing.currentPlan')}</p>
                                <div className="flex items-center gap-3">
                                    <h2 className="text-3xl font-bold text-text-primary">{subscription.plan.name}</h2>
                                    <span className="text-xs font-medium px-2.5 py-1 bg-success/10 text-success rounded-full">
                                        {subscription.status}
                                    </span>
                                </div>
                                <p className="text-sm text-text-secondary mt-2">
                                    {subscription.billingCycle === 'YEARLY'
                                        ? `$${subscription.plan.yearlyPrice}${t('billing.perYear')}`
                                        : `$${subscription.plan.monthlyPrice}${t('billing.perMonth')}`}
                                </p>
                                {currentPlanName !== 'FREE' && (
                                    <button
                                        onClick={handleCancel}
                                        disabled={working}
                                        className="mt-4 flex items-center gap-2 px-3 py-2 bg-danger/10 hover:bg-danger/20 text-danger text-sm rounded-xl font-medium transition-all active:scale-95 disabled:opacity-50"
                                    >
                                        <XCircle className="w-4 h-4" />
                                        {t('billing.cancelSubscription')}
                                    </button>
                                )}
                            </div>

                            <div className="bg-surface border border-border rounded-2xl p-6">
                                <div className="flex items-center justify-between mb-3">
                                    <p className="text-sm text-text-secondary flex items-center gap-2">
                                        <Users className="w-4 h-4" /> {t('billing.usersUsage')}
                                    </p>
                                    <span className="text-sm font-semibold text-text-primary">
                                        {subscription.currentUsers} / {subscription.maxUsers > 0 ? subscription.maxUsers : t('billing.unlimited')}
                                    </span>
                                </div>
                                <div className="h-2 bg-black/5 dark:bg-white/10 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-primary rounded-full transition-all"
                                        style={{ width: `${usagePercent(subscription.currentUsers, subscription.maxUsers)}%` }}
                                    ></div>
                                </div>
                            </div>

                            <div className="bg-surface border border-border rounded-2xl p-6">
                                <div className="flex items-center justify-between mb-3">
                                    <p className="text-sm text-text-secondary flex items-center gap-2">
                                        <HardDrive className="w-4 h-4" /> {t('billing.storageUsage')}
                                    </p>
                                    <span className="text-sm font-semibold text-text-primary">
                                        {subscription.storageUsedMb} MB / {subscription.maxStorageMb} MB
                                    </span>
                                </div>
                                <div className="h-2 bg-black/5 dark:bg-white/10 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-secondary rounded-full transition-all"
                                        style={{ width: `${usagePercent(subscription.storageUsedMb, subscription.maxStorageMb)}%` }}
                                    ></div>
                                </div>
                            </div>
                        </div>

                        {/* Billing cycle toggle */}
                        <div className="flex justify-center">
                            <div className="flex bg-black/5 dark:bg-white/5 p-1 rounded-xl">
                                {['MONTHLY', 'YEARLY'].map((cycle) => (
                                    <button
                                        key={cycle}
                                        onClick={() => setBillingCycle(cycle)}
                                        className={cn(
                                            "px-6 py-2 rounded-lg text-sm font-medium transition-all",
                                            billingCycle === cycle
                                                ? "bg-surface text-text-primary shadow-sm"
                                                : "text-text-secondary hover:text-text-primary"
                                        )}
                                    >
                                        {cycle === 'MONTHLY' ? t('billing.monthly') : t('billing.yearly')}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Plans */}
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            {plans.map((plan) => {
                                const isCurrent = plan.name === currentPlanName;
                                const price = billingCycle === 'YEARLY' ? plan.yearlyPrice : plan.monthlyPrice;
                                return (
                                    <div
                                        key={plan.id}
                                        className={cn(
                                            "bg-surface border rounded-2xl p-6 flex flex-col transition-all",
                                            isCurrent ? "border-primary ring-2 ring-primary/20" : "border-border hover:shadow-xl hover:shadow-black/5"
                                        )}
                                    >
                                        <h3 className="text-lg font-bold text-text-primary">{plan.name}</h3>
                                        <div className="mt-2 mb-4">
                                            <span className="text-3xl font-bold text-text-primary">${price}</span>
                                            <span className="text-sm text-text-secondary">
                                                {billingCycle === 'YEARLY' ? t('billing.perYear') : t('billing.perMonth')}
                                            </span>
                                        </div>
                                        <ul className="space-y-2 text-sm text-text-secondary flex-1 mb-6">
                                            <li className="flex items-center gap-2">
                                                <Check className="w-4 h-4 text-success shrink-0" />
                                                {plan.maxUsers > 0 ? `${plan.maxUsers} ${t('billing.usersUsage')}` : `${t('billing.unlimited')} ${t('billing.usersUsage')}`}
                                            </li>
                                            <li className="flex items-center gap-2">
                                                <Check className="w-4 h-4 text-success shrink-0" />
                                                {plan.maxStorageMb >= 1024
                                                    ? `${Math.round(plan.maxStorageMb / 1024)} GB ${t('billing.storageUsage')}`
                                                    : `${plan.maxStorageMb} MB ${t('billing.storageUsage')}`}
                                            </li>
                                        </ul>
                                        <button
                                            onClick={() => handleUpgradeClick(plan.name)}
                                            disabled={isCurrent || working}
                                            className={cn(
                                                "w-full py-2.5 rounded-xl text-sm font-medium transition-all active:scale-95",
                                                isCurrent
                                                    ? "bg-black/5 dark:bg-white/10 text-text-secondary cursor-default"
                                                    : "bg-primary hover:bg-primary-hover text-white shadow-lg shadow-primary/20"
                                            )}
                                        >
                                            {isCurrent ? t('billing.currentPlan') : t('billing.upgrade')}
                                        </button>
                                    </div>
                                );
                            })}
                        </div>

                        {/* Invoices */}
                        <div className="bg-surface border border-border rounded-2xl p-6">
                            <h2 className="text-lg font-bold text-text-primary flex items-center gap-2 mb-4">
                                <Receipt className="w-5 h-5 text-primary" />
                                {t('billing.invoices')}
                            </h2>
                            {invoices.length === 0 ? (
                                <p className="text-sm text-text-secondary py-6 text-center">{t('common.noData')}</p>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-sm">
                                        <thead>
                                            <tr className="text-left text-text-secondary border-b border-border">
                                                <th className="py-3 pr-4 font-medium">{t('billing.invoiceNumber')}</th>
                                                <th className="py-3 pr-4 font-medium">{t('org.plan')}</th>
                                                <th className="py-3 pr-4 font-medium">{t('billing.amount')}</th>
                                                <th className="py-3 pr-4 font-medium">{t('common.status')}</th>
                                                <th className="py-3 font-medium">{t('billing.issuedAt')}</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {invoices.map((inv) => (
                                                <tr key={inv.id} className="border-b border-border/50 last:border-0 text-text-primary">
                                                    <td className="py-3 pr-4 font-mono text-xs">{inv.invoiceNumber}</td>
                                                    <td className="py-3 pr-4">{inv.planName}</td>
                                                    <td className="py-3 pr-4">${inv.amount}</td>
                                                    <td className="py-3 pr-4">
                                                        <span className={cn(
                                                            "text-xs font-medium px-2.5 py-1 rounded-full",
                                                            inv.status === 'PAID' ? "bg-success/10 text-success" : "bg-warning/10 text-warning"
                                                        )}>
                                                            {inv.status}
                                                        </span>
                                                    </td>
                                                    <td className="py-3 text-text-secondary">{new Date(inv.issuedAt).toLocaleString()}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>

            {/* Confirmation Modal */}
            {confirmModal.isOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-surface border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl">
                        <h3 className="text-xl font-bold text-text-primary mb-2">
                            {t('billing.confirmUpgradeTitle')}
                        </h3>
                        <p className="text-text-secondary mb-6">
                            {t('billing.confirmUpgradeDesc', { plan: confirmModal.planName })}
                        </p>
                        <div className="flex justify-end gap-3">
                            <button
                                onClick={() => setConfirmModal({ isOpen: false, planName: null })}
                                disabled={working}
                                className="px-4 py-2 rounded-xl text-sm font-medium text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors disabled:opacity-50"
                            >
                                {t('common.cancel')}
                            </button>
                            <button
                                onClick={confirmUpgrade}
                                disabled={working}
                                className="px-4 py-2 rounded-xl text-sm font-medium bg-primary hover:bg-primary-hover text-white shadow-lg shadow-primary/20 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center min-w-[120px]"
                            >
                                {working ? (
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    t('billing.confirmUpgradeBtn')
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default Billing;
