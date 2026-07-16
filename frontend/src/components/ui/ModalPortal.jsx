import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

// Reference-counted so stacked/overlapping modals don't unlock scroll early
// when one of them closes while another is still open.
let scrollLockCount = 0;

function lockPageScroll() {
    scrollLockCount += 1;
    if (scrollLockCount === 1) {
        const el = document.getElementById('page-scroll');
        if (el) el.style.overflow = 'hidden';
    }
}

function unlockPageScroll() {
    scrollLockCount = Math.max(0, scrollLockCount - 1);
    if (scrollLockCount === 0) {
        const el = document.getElementById('page-scroll');
        if (el) el.style.overflow = '';
    }
}

/**
 * Renders children into the `#modal-root` DOM element (or document.body as
 * fallback) using a React Portal, ensuring modals stack above the rest of
 * the page regardless of their position in the component tree. Also locks
 * background page scrolling for as long as it stays mounted.
 * @param {{ children: React.ReactNode }} props
 * @returns {React.ReactPortal|null}
 */
const ModalPortal = ({ children }) => {
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
        lockPageScroll();
        return () => {
            setMounted(false);
            unlockPageScroll();
        };
    }, []);

    if (!mounted) return null;

    const root = document.getElementById('modal-root') || document.body;

    return createPortal(children, root);
};

export default ModalPortal;
