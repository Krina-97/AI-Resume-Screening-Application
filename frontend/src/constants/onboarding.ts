export const ONBOARDING_KEY = 'hiresight_onboarding_v1';

export type OnboardingState = {
  jobCreated: boolean;
  resumeUploaded: boolean;
  dismissed: boolean;
};

export const defaultOnboarding = (): OnboardingState => ({
  jobCreated: false,
  resumeUploaded: false,
  dismissed: false
});
