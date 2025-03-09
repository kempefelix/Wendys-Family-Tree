import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  image?: string;
  owner?: Owner;
  parentFemale?: Horse;
  parentMale?: Horse;
}

export interface HorseSearch {
  name?: string;
  description?: string;
  bornBefore?: string;
  sex?: string;
  ownerName?: string;
}

export interface HorseCreate {
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  image?: string;
  ownerId?: number;
  parentFemaleId?: number;
  parentMaleId?: number;
}

export function convertFromHorseToCreate(horse: Horse): HorseCreate {
  return {
    name: horse.name,
    description: horse.description,
    dateOfBirth: horse.dateOfBirth,
    sex: horse.sex,
    image: horse.image,
    ownerId: horse.owner?.id,
    parentFemaleId: horse.parentFemale ? horse.parentFemale.id : undefined,
    parentMaleId: horse.parentMale ? horse.parentMale.id : undefined
  };
}