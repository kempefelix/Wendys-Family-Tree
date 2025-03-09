export interface Owner {
  id?: number;
  firstName: string;
  lastName: string;
  email?: string;
  description?: string;
}

export interface OwnerCreate {
  firstName: string;
  lastName: string;
  email?: string;
  description?: string;
}