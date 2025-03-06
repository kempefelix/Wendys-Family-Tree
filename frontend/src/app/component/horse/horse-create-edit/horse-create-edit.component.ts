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
    FormsModule
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
    owner: undefined
  };
  horseBirthDateIsSet = false;


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
        },
        error: err => {
          console.error('Error loading horse for edit', err);
          this.notification.error('Could not load horse for editing.', 'Error');
        }
      });
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
