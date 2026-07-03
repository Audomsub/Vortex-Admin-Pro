let activeDialog = null;
let listeners = [];

const notify = () => {
    listeners.forEach(l => l(activeDialog));
};

export const registerDialogListener = (fn) => {
    listeners.push(fn);
    return () => {
        listeners = listeners.filter(l => l !== fn);
    };
};

export const showConfirm = (message, title = 'Confirm Action') => {
    return new Promise((resolve) => {
        const dialog = {
            type: 'confirm',
            title,
            message,
            onConfirm: () => {
                activeDialog = null;
                notify();
                resolve(true);
            },
            onCancel: () => {
                activeDialog = null;
                notify();
                resolve(false);
            }
        };
        activeDialog = dialog;
        notify();
    });
};

export const showAlert = (message, title = 'Alert') => {
    return new Promise((resolve) => {
        const dialog = {
            type: 'alert',
            title,
            message,
            onConfirm: () => {
                activeDialog = null;
                notify();
                resolve(true);
            },
            onCancel: () => {
                activeDialog = null;
                notify();
                resolve(true);
            }
        };
        activeDialog = dialog;
        notify();
    });
};

window.vortexConfirm = showConfirm;
window.vortexAlert = showAlert;
