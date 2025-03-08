import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse, convertFromHorseToCreate} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {CommonModule} from '@angular/common';

export enum HorseCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    CommonModule
  ],
  standalone: true,
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    image: '',
    owner: undefined,
    parentFemale: undefined,
    parentMale: undefined
  };  
  horseBirthDateIsSet = false;
  
  femaleHorses: Horse[] = [];
  maleHorses: Horse[] = [];

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      default:
        return '?';
    }
  }
  
  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Save Changes';
      default:
        return '?';
    }
  }

  public get horseBirthDateText(): string {
    if (!this.horseBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.horse.dateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date == null || date === '') {
      this.horseBirthDateIsSet = false;
    } else {
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }

  get sex(): string {
    switch (this.horse.sex) {
      case Sex.male:
        return 'Male';
      case Sex.female:
        return 'Female';
      default:
        return '';
    }
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'updated';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      
      if (this.mode === HorseCreateEditMode.edit) {
        this.route.paramMap.subscribe(params => {
          const horseId = params.get('id');
          if (horseId) {
            this.loadHorseForEdit(+horseId);
          }
        });
      } else {
        this.loadAvailableParents();
      }
    });
  }
  
  private loadAvailableParents(): void {
    // Load female horses for dropdown (excluding current horse in edit mode)
    this.service.getAllByGender('FEMALE', this.horse.id).subscribe({
      next: (horses: Horse[]) => {
        this.femaleHorses = horses;
        console.log('Loaded female horses:', horses);
      },
      error: err => {
        console.error('Error loading female horses', err);
        this.notification.error('Could not load female parents.', 'Error');
      }
    });
    
    // Load male horses for dropdown (excluding current horse in edit mode)
    this.service.getAllByGender('MALE', this.horse.id).subscribe({
      next: (horses: Horse[]) => {
        this.maleHorses = horses;
        console.log('Loaded male horses:', horses);
      },
      error: err => {
        console.error('Error loading male horses', err);
        this.notification.error('Could not load male parents.', 'Error');
      }
    });
  }
  
  private loadHorseForEdit(id: number): void {
    this.service.getById(id).subscribe({
      next: (horseDto: Horse) => {
        this.horse = {
          ...horseDto,
          dateOfBirth: new Date(horseDto.dateOfBirth)
        };
        this.horseBirthDateIsSet = true;
        console.log('Loaded horse for edit:', this.horse);
        this.loadAvailableParents();
        this.handleParentReferences();
      },
      error: err => {
        console.error('Error loading horse for edit', err);
        this.notification.error('Could not load horse for editing.', 'Error');
      }
    });
  }
  
  private handleParentReferences(): void {
    if (this.horse.parentFemale && typeof this.horse.parentFemale === 'number') {
      const parentFemaleId = this.horse.parentFemale as unknown as number;
      this.service.getById(parentFemaleId).subscribe({
        next: (parentFemale: Horse) => {
          this.horse.parentFemale = parentFemale;
          if (!this.femaleHorses.some(horse => horse.id === parentFemale.id)) {
            this.femaleHorses.push(parentFemale);
          }
        },
        error: err => {
          console.error("Error loading parent female", err);
        }
      });
    }
    
    if (this.horse.parentMale && typeof this.horse.parentMale === 'number') {
      const parentMaleId = this.horse.parentMale as unknown as number;
      this.service.getById(parentMaleId).subscribe({
        next: (parentMale: Horse) => {
          this.horse.parentMale = parentMale;
          if (!this.maleHorses.some(horse => horse.id === parentMale.id)) {
            this.maleHorses.push(parentMale);
          }
        },
        error: err => {
          console.error("Error loading parent male", err);
        }
      });
    }
  }

  compareHorses(horse1: Horse | undefined, horse2: Horse | undefined): boolean {
    if (!horse1 || !horse2) {
      return horse1 === horse2;
    }
    return horse1.id === horse2.id;
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined | string): string {
    if (!owner) {
      return '';
    }
    if (typeof owner === 'string') {
      return owner;
    }
    return `${owner.firstName} ${owner.lastName}`;
  }

  public formatHorseName(horse: Horse | null | undefined | string): string {
    if (!horse) {
      return '';
    }
    if (typeof horse === 'string') {
      return horse;
    }
    return horse.name;
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
  
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(
            convertFromHorseToCreate(this.horse)
          );
          break;
  
        case HorseCreateEditMode.edit:
          observable = this.service.update(this.horse);
          break;
  
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
  
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error creating/updating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create/Update Horse', {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
  }
}