import { useState, useEffect, useCallback } from 'react';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import {
    CreditCard, Check, Users, HardDrive, Building2, Receipt, XCircle, QrCode, Wallet, AlertTriangle
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
    const [discounts, setDiscounts] = useState({ loyaltyDiscountEligible: false, firstYearDiscountEligible: false });
    const [paymentProvider, setPaymentProvider] = useState('STRIPE');
    const [cancelModalOpen, setCancelModalOpen] = useState(false);
    const [errorModal, setErrorModal] = useState({ isOpen: false, message: '' });

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
            const [subRes, invRes, discountRes] = await Promise.all([
                billingService.getSubscription(orgId),
                billingService.getInvoices(orgId),
                billingService.getDiscounts(orgId),
            ]);
            setSubscription(subRes.data.data);
            setInvoices(invRes.data.data || []);
            setDiscounts(discountRes.data.data || { loyaltyDiscountEligible: false, firstYearDiscountEligible: false });
        } catch (error) {
            console.error('Failed to load billing:', error);
            setSubscription(null);
            setInvoices([]);
            setDiscounts({ loyaltyDiscountEligible: false, firstYearDiscountEligible: false });
        } finally {
            setLoading(false);
        }
    }, [orgId]);

    useEffect(() => {
        loadBilling();
    }, [loadBilling]);

    const handleUpgradeClick = (planName) => {
        setConfirmModal({ isOpen: true, planName });
        setPaymentProvider('STRIPE');
    };

    async function confirmUpgrade() {
        if (!confirmModal.planName) return;
        setWorking(true);
        try {
            await billingService.upgrade(orgId, confirmModal.planName, billingCycle, paymentProvider);
            await loadBilling();
            fetchOrganizations();
            setConfirmModal({ isOpen: false, planName: null });
        } catch (error) {
            setErrorModal({ isOpen: true, message: error.response?.data?.message || t('common.error') });
        } finally {
            setWorking(false);
        }
    };

    const handleCancelClick = () => {
        setCancelModalOpen(true);
    };

    async function confirmCancel() {
        setCancelModalOpen(false);
        setWorking(true);
        try {
            await billingService.cancel(orgId);
            await loadBilling();
            fetchOrganizations();
        } catch (error) {
            setErrorModal({ isOpen: true, message: error.response?.data?.message || t('common.error') });
        } finally {
            setWorking(false);
        }
    };

    const usagePercent = (used, max) => {
        if (!max || max <= 0) return 0;
        return Math.min(100, Math.round((used / max) * 100));
    };

    const currentPlanName = subscription?.plan?.name;

    const selectedPlan = plans.find(p => p.name === confirmModal.planName);
    let modalPriceText = '';
    if (selectedPlan) {
        const basePrice = billingCycle === 'YEARLY' ? selectedPlan.yearlyPrice : selectedPlan.monthlyPrice;
        const isPaid = ['PRO', 'BUSINESS', 'ENTERPRISE'].includes(selectedPlan.name);
        let finalPrice = basePrice;
        if (isPaid) {
            let discountFactor = 1;
            let applied = [];
            if (discounts.loyaltyDiscountEligible) applied.push('LOYALTY');
            if (discounts.firstYearDiscountEligible && billingCycle === 'YEARLY') applied.push('FIRST_YEAR');
            if (applied.includes('LOYALTY') && applied.includes('FIRST_YEAR')) discountFactor = 0.70;
            else if (applied.includes('FIRST_YEAR')) discountFactor = 0.80;
            else if (applied.includes('LOYALTY')) discountFactor = 0.90;
            finalPrice = Math.round((basePrice * discountFactor) * 100) / 100;
        }
        modalPriceText = `$${finalPrice} ${billingCycle === 'YEARLY' ? t('billing.perYear') : t('billing.perMonth')}`;
    }

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
                                        onClick={handleCancelClick}
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
                                const originalPrice = billingCycle === 'YEARLY' ? plan.yearlyPrice : plan.monthlyPrice;
                                
                                const isPaidPlan = ['PRO', 'BUSINESS', 'ENTERPRISE'].includes(plan.name);
                                let discountFactor = 1;
                                let appliedDiscounts = [];

                                if (isPaidPlan) {
                                    if (discounts.loyaltyDiscountEligible) {
                                        appliedDiscounts.push('LOYALTY');
                                    }
                                    if (discounts.firstYearDiscountEligible && billingCycle === 'YEARLY') {
                                        appliedDiscounts.push('FIRST_YEAR');
                                    }

                                    if (appliedDiscounts.includes('LOYALTY') && appliedDiscounts.includes('FIRST_YEAR')) {
                                        discountFactor = 0.70; // 30% off
                                    } else if (appliedDiscounts.includes('FIRST_YEAR')) {
                                        discountFactor = 0.80; // 20% off
                                    } else if (appliedDiscounts.includes('LOYALTY')) {
                                        discountFactor = 0.90; // 10% off
                                    }
                                }

                                const finalPrice = isPaidPlan ? Math.round((originalPrice * discountFactor) * 100) / 100 : originalPrice;
                                const hasDiscount = finalPrice < originalPrice;

                                return (
                                    <div
                                        key={plan.id}
                                        className={cn(
                                            "bg-surface border rounded-2xl p-6 flex flex-col transition-all relative overflow-hidden",
                                            isCurrent ? "border-primary ring-2 ring-primary/20" : "border-border hover:shadow-xl hover:shadow-black/5"
                                        )}
                                    >
                                        <h3 className="text-lg font-bold text-text-primary">{plan.name}</h3>

                                        {/* Discount Badge */}
                                        {hasDiscount && (
                                            <div className="mt-2 flex flex-wrap gap-1.5">
                                                {appliedDiscounts.includes('LOYALTY') && appliedDiscounts.includes('FIRST_YEAR') ? (
                                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 text-white shadow-sm border border-indigo-400/20">
                                                        🎉 Combo 30% Off
                                                    </span>
                                                ) : appliedDiscounts.includes('FIRST_YEAR') ? (
                                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-sm border border-emerald-400/20">
                                                        ✨ First Year 20% Off
                                                    </span>
                                                ) : (
                                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gradient-to-r from-blue-500 to-cyan-500 text-white shadow-sm border border-blue-400/20">
                                                        💝 Loyalty 10% Off
                                                    </span>
                                                )}
                                            </div>
                                        )}

                                        <div className="mt-2 mb-4 flex items-baseline gap-2">
                                            {hasDiscount ? (
                                                <>
                                                    <span className="text-3xl font-extrabold text-text-primary">${finalPrice}</span>
                                                    <span className="text-sm line-through text-text-secondary font-medium">${originalPrice}</span>
                                                </>
                                            ) : (
                                                <span className="text-3xl font-extrabold text-text-primary">${originalPrice}</span>
                                            )}
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
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-surface border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl">
                        <h3 className="text-xl font-bold text-text-primary mb-2">
                            {confirmModal.planName === 'FREE' ? 'Confirm Downgrade' : t('billing.confirmUpgradeTitle')}
                        </h3>
                        <div className="text-text-secondary mb-6">
                            <p>
                                {confirmModal.planName === 'FREE'
                                    ? 'Are you sure you want to change to the FREE plan? Your current subscription benefits will be cancelled.'
                                    : t('billing.confirmUpgradeDesc', { plan: confirmModal.planName })}
                            </p>
                            {confirmModal.planName !== 'FREE' && modalPriceText && (
                                <p className="mt-2 font-semibold text-text-primary text-lg">
                                    Price: {modalPriceText}
                                </p>
                            )}
                        </div>

                        {/* Select Payment Method */}
                        {confirmModal.planName !== 'FREE' && (
                            <div className="mb-6">
                                <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-2.5">
                                    Select Payment Method
                                </label>
                                <div className="grid grid-cols-3 gap-3">
                                    {[
                                        { id: 'STRIPE', name: 'Credit Card', icon: CreditCard },
                                        { id: 'PROMPTPAY', name: 'PromptPay', icon: QrCode },
                                        { id: 'PAYPAL', name: 'PayPal', icon: Wallet }
                                    ].map((prov) => {
                                        const IconComp = prov.icon;
                                        const isSel = paymentProvider === prov.id;
                                        return (
                                            <button
                                                key={prov.id}
                                                type="button"
                                                onClick={() => setPaymentProvider(prov.id)}
                                                className={cn(
                                                    "flex flex-col items-center justify-center p-3 rounded-xl border text-center transition-all active:scale-95",
                                                    isSel
                                                        ? "border-primary bg-primary/5 text-primary ring-2 ring-primary/20"
                                                        : "border-border hover:border-text-secondary/50 dark:hover:border-white/20 text-text-secondary hover:text-text-primary bg-transparent"
                                                )}
                                            >
                                                <IconComp className="w-5 h-5 mb-1.5" />
                                                <span className="text-xs font-medium">{prov.name}</span>
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        )}

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
                                    confirmModal.planName === 'FREE' ? 'Confirm Switch' : t('billing.confirmUpgradeBtn')
                                )}
                            </button>
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Cancellation Modal */}
            {cancelModalOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
                    <div className="bg-surface border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl scale-100 transition-all">
                        <div className="flex items-center gap-3 mb-4 text-danger">
                            <div className="p-2 bg-danger/10 rounded-xl">
                                <AlertTriangle className="w-6 h-6" />
                            </div>
                            <h3 className="text-xl font-bold text-text-primary">
                                {t('billing.cancelSubscription')}
                            </h3>
                        </div>
                        <p className="text-text-secondary mb-6 text-sm leading-relaxed">
                            Are you sure you want to cancel your subscription? Your organization will immediately downgrade to the FREE plan, and you will lose access to premium features.
                        </p>
                        <div className="flex justify-end gap-3">
                            <button
                                onClick={() => setCancelModalOpen(false)}
                                disabled={working}
                                className="px-4 py-2 rounded-xl text-sm font-medium text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors disabled:opacity-50"
                            >
                                Keep Subscription
                            </button>
                            <button
                                onClick={confirmCancel}
                                disabled={working}
                                className="px-4 py-2 rounded-xl text-sm font-medium bg-danger hover:bg-danger/90 text-white shadow-lg shadow-danger/20 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center min-w-[120px]"
                            >
                                {working ? (
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    "Yes, Cancel"
                                )}
                            </button>
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Error Alert Modal */}
            {errorModal.isOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-surface border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl">
                        <div className="flex items-center gap-3 mb-4 text-danger">
                            <div className="p-2 bg-danger/10 rounded-xl">
                                <XCircle className="w-6 h-6" />
                            </div>
                            <h3 className="text-xl font-bold text-text-primary">
                                Error occurred
                            </h3>
                        </div>
                        <p className="text-text-secondary mb-6 text-sm leading-relaxed font-medium">
                            {errorModal.message}
                        </p>
                        <div className="flex justify-end">
                            <button
                                onClick={() => setErrorModal({ isOpen: false, message: '' })}
                                className="px-5 py-2 rounded-xl text-sm font-medium bg-primary hover:bg-primary-hover text-white shadow-lg shadow-primary/20 transition-all active:scale-95"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Billing;
