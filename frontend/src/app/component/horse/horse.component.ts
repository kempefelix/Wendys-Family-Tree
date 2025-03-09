import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;

  searchName: string = '';
  searchDescription: string = '';
  searchDateOfBirth: string = '';
  searchSex: string = '';
  searchOwner: string = '';

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadHorses();
  }

  reloadHorses() {
    const hasCriteria = this.searchName || this.searchDescription ||
      this.searchDateOfBirth || this.searchSex || this.searchOwner;

    let observable: Observable<Horse[]>;
    if (hasCriteria) {
      observable = this.service.search({
        name: this.searchName,
        description: this.searchDescription,
        bornBefore: this.searchDateOfBirth ? this.searchDateOfBirth : undefined,
        sex: this.searchSex,
        ownerName: this.searchOwner
      });
    } else {
      observable = this.service.getAll();
    }

    observable.subscribe({
      next: (data: Horse[]) => {
        this.horses = data;
        this.bannerError = null;
      },
      error: (error: any) => {
        console.error('Error fetching horses', error);
        this.bannerError = 'Could not fetch horses: ' + error.message;
        const errorMessage = error.status === 0
          ? 'Is the backend up?'
          : error.message.message;
        this.notification.error(errorMessage, 'Could Not Fetch Horses');
      }
    });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth.toLocaleDateString();
  }

  deleteHorse(horse: Horse) {
    if (horse.id == null) {
      console.error('Horse ID is undefined, cannot delete horse.');
      return;
    }
    this.service.delete(horse.id!).subscribe({
      next: () => {
        this.notification.success(`Horse ${horse.name} deleted successfully.`);
        this.reloadHorses();
      },
      error: (error: any) => {
        console.error('Error deleting horse', error);
        this.notification.error('Could not delete horse.', 'Error');
      }
    });
  }
}