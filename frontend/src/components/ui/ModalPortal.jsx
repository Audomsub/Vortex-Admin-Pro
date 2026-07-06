import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

/**
 * Renders children into the `#modal-root` DOM element (or document.body as
 * fallback) using a React Portal, ensuring modals stack above the rest of
 * the page regardless of their position in the component tree.
 * @param {{ children: React.ReactNode }} props
 * @returns {React.ReactPortal|null}
 */
const ModalPortal = ({ children }) => {
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
        return () => setMounted(false);
    }, []);

    if (!mounted) return null;

    const root = document.getElementById('modal-root') || document.body;

    return createPortal(children, root);
};

export default ModalPortal;
