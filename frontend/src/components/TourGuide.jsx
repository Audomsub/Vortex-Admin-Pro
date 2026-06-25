import { useState, useEffect } from 'react';
import { Joyride, STATUS } from 'react-joyride';

const TourGuide = () => {
    const [run, setRun] = useState(false);

    useEffect(() => {
        // Check if the user has already seen the tour
        const hasSeenTour = localStorage.getItem('hasSeenTour');
        if (!hasSeenTour) {
            // Add a small delay to let the UI render completely
            const timer = setTimeout(() => {
                setRun(true);
                // Mark as seen immediately so navigating/refreshing won't trigger it again
                localStorage.setItem('hasSeenTour', 'true');
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, []);

    const steps = [
        {
            target: 'body',
            content: 'Welcome to Vortex Admin Pro! Let us show you around your new dashboard.',
            placement: 'center',
            disableBeacon: true,
        },
        {
            target: 'aside nav a[href="/dashboard"]',
            content: 'Here is your Dashboard where you can see the overall metrics and charts.',
            placement: 'right',
        },
        {
            target: 'aside nav a[href="/users"]',
            content: 'Manage your users, assign roles, and handle permissions from the User Management screen.',
            placement: 'right',
        },
        {
            target: 'aside nav a[href="/tasks"]',
            content: 'Keep track of what needs to be done. You can create, assign, and update tasks here.',
            placement: 'right',
        },
        {
            target: '.header-profile-menu', // Assuming there's a profile menu in the header
            content: 'Click here to edit your profile, change your theme, or adjust 2FA security settings.',
            placement: 'bottom',
        }
    ];

    const handleJoyrideCallback = (data) => {
        const { status } = data;
        const finishedStatuses = [STATUS.FINISHED, STATUS.SKIPPED];

        if (finishedStatuses.includes(status)) {
            setRun(false);
            localStorage.setItem('hasSeenTour', 'true');
        }
    };

    return (
        <Joyride
            callback={handleJoyrideCallback}
            continuous
            hideCloseButton
            run={run}
            scrollToFirstStep
            showProgress
            showSkipButton
            steps={steps}
            styles={{
                options: {
                    zIndex: 10000,
                    primaryColor: '#4f46e5',
                    textColor: '#1f2937',
                    backgroundColor: '#ffffff',
                    arrowColor: '#ffffff',
                },
                tooltipContainer: {
                    textAlign: 'left'
                },
                buttonNext: {
                    backgroundColor: '#4f46e5',
                    borderRadius: '8px',
                    padding: '8px 16px'
                },
                buttonBack: {
                    marginRight: '10px',
                    color: '#6b7280'
                },
                buttonSkip: {
                    color: '#6b7280'
                }
            }}
        />
    );
};

export default TourGuide;
