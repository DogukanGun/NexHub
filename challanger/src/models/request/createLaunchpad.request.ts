type CreateLaunchpadRequest = { 
    name: string;
    description: string;
    price: number;
    createdBy: string;
    projectSocialLink: string[];
}

export default CreateLaunchpadRequest;