import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OwnerService } from 'src/app/service/owner.service';
import { ToastrService } from 'ngx-toastr';
import { Owner } from 'src/app/dto/owner';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';


@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss'],
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent,
    CommonModule
  ],
})


export class OwnerComponent implements OnInit {
  owners: Owner[] = [];
  bannerError: string | null = null;

  constructor(
    private ownerService: OwnerService,
    private router: Router,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners(): void {
    this.ownerService.getAll().subscribe({
      next: (data: Owner[]) => {
        this.owners = data;
        this.bannerError = null;
      },
      error: (error: any) => {
        console.error('Error fetching owners', error);
        this.bannerError = 'Could not fetch owners: ' + error.message;
        this.notification.error(error.message, 'Error');
      },
    });
  }

  deleteOwner(owner: Owner): void {
    if (!owner.id) return;
    this.ownerService.delete(owner.id).subscribe({
      next: () => {
        this.notification.success(
          `Owner ${owner.firstName} ${owner.lastName} deleted successfully.`
        );
        this.reloadOwners();
      },
      error: (error: any) => {
        console.error('Error deleting owner', error);
        this.notification.error('Could not delete owner.', 'Error');
      },
    });
  }

  trackOwner(index: number, owner: Owner): number | undefined {
    return owner.id;
  }
}
