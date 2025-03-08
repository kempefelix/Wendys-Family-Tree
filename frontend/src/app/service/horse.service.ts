import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate, HorseSearch}  from '../dto/horse';
import {formatIsoDate} from "../utils/date-helper";
import { HttpParams } from '@angular/common/http';


const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {
  private baseUri = 'http://localhost:8080/horses';

  constructor(
    private http: HttpClient
  ) {
  }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri)
      .pipe(
        map(horses => horses.map(this.fixHorseDate))
      );
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: HorseCreate): Observable<Horse> {
    console.log(horse);
    // Cast the object to any, so that we can circumvent the type checker.
    // We _need_ the date to be a string here, and just passing the object with the
    // "type error" to the HTTP client is unproblematic
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);

    return this.http.post<Horse>(
      baseUri,
      horse
    ).pipe(
      map(this.fixHorseDate)
    );
  }

  private fixHorseDate(horse: Horse): Horse {
    // Parse the string to a Date
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }

  update(horse: Horse): Observable<Horse> {
    const updateDto = {
      name: horse.name,
      description: horse.description,
      dateOfBirth: horse.dateOfBirth,
      sex: horse.sex,
      image: horse.image,
      ownerId: horse.owner?.id,
      parentFemaleId: horse.parentFemale?.id,
      parentMaleId: horse.parentMale?.id
    };
    return this.http.put<Horse>(`${baseUri}/${horse.id}`, updateDto);
  }
  
  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }
  
  /**
   * Search for horses by name and gender, excluding a specific horse ID
   * @param input The name to search for
   * @param gender The gender to filter by (MALE or FEMALE)
   * @param currentHorseId Optional ID to exclude from results
   */
  searchByGender(input: string, gender: string, currentHorseId?: number): Observable<Horse[]> {
    return this.getAll().pipe(
      map((horses: Horse[]): Horse[] =>
        horses.filter((horse: Horse): boolean =>
          horse.sex === gender &&
          horse.name.toLowerCase().includes(input.toLowerCase()) &&
          (!currentHorseId || horse.id !== currentHorseId)
        )
      )
    );
  }

  /**
   * Get all horses of a specific gender, excluding a specific horse ID
   * @param gender The gender to filter by (MALE or FEMALE)
   * @param currentHorseId Optional ID to exclude from results
   */
  getAllByGender(gender: string, currentHorseId?: number): Observable<Horse[]> {
    return this.getAll().pipe(
      map((horses: Horse[]): Horse[] =>
        horses.filter((horse: Horse): boolean =>
          horse.sex === gender &&
          (!currentHorseId || horse.id !== currentHorseId)
        )
      )
    );
  }

  search(searchParams: HorseSearch): Observable<Horse[]> {
    let params = new HttpParams();
    
    if (searchParams.name) {
      params = params.set('name', searchParams.name);
    }
    
    if (searchParams.description) {
      params = params.set('description', searchParams.description);
    }
    
    if (searchParams.bornBefore) {
      params = params.set('bornBefore', searchParams.bornBefore);
    }
    
    if (searchParams.sex) {
      params = params.set('sex', searchParams.sex);
    }
    
    if (searchParams.ownerName) {
      params = params.set('ownerName', searchParams.ownerName);
    }
  
    return this.http.get<Horse[]>(this.baseUri, { params })
      .pipe(
        map(horses => horses.map(this.fixHorseDate))
      );
  }
}