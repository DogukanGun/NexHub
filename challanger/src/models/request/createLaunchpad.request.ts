/**
 * Interface for creating a new launchpad
 */
export default interface CreateLaunchpadRequest { 
    name: string;
    description: string;
    price: number;
    createdBy: string;
    projectSocialLink: string[];
}