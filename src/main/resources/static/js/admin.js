document.addEventListener('DOMContentLoaded', function() {
    // Handle shop approval
    document.querySelectorAll('.approve-shop').forEach(button => {
        button.addEventListener('click', function() {
            const shopId = this.getAttribute('data-shop-id');
            approveShop(shopId);
        });
    });

    // Handle shop rejection
    document.querySelectorAll('.reject-shop').forEach(button => {
        button.addEventListener('click', function() {
            const shopId = this.getAttribute('data-shop-id');
            rejectShop(shopId);
        });
    });

    // Handle shop edit
    document.querySelectorAll('.edit-shop').forEach(button => {
        button.addEventListener('click', function() {
            const shopId = this.getAttribute('data-shop-id');
            editShop(shopId);
        });
    });

    // Handle shop status toggle
    document.querySelectorAll('.toggle-shop-status').forEach(button => {
        button.addEventListener('click', function() {
            const shopId = this.getAttribute('data-shop-id');
            const currentStatus = this.getAttribute('data-shop-status');
            toggleShopStatus(shopId, currentStatus);
        });
    });

    // Handle shop deletion
    document.querySelectorAll('.delete-shop').forEach(button => {
        button.addEventListener('click', function() {
            const shopId = this.getAttribute('data-shop-id');
            deleteShop(shopId);
        });
    });
});

function approveShop(shopId) {
    if (confirm('Are you sure you want to approve this shop?')) {
        fetch(`/admin/shops/${shopId}/approve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert('Failed to approve shop');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while approving the shop');
        });
    }
}

function rejectShop(shopId) {
    if (confirm('Are you sure you want to reject this shop?')) {
        fetch(`/admin/shops/${shopId}/reject`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert('Failed to reject shop');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while rejecting the shop');
        });
    }
}

function editShop(shopId) {
    // TODO: Implement edit shop functionality
    alert('Edit shop functionality coming soon!');
}

function toggleShopStatus(shopId, currentStatus) {
    const newStatus = currentStatus === 'ACTIVE' ? 'deactivate' : 'activate';
    if (confirm(`Are you sure you want to ${newStatus} this shop?`)) {
        fetch(`/admin/shops/${shopId}/${newStatus}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert(`Failed to ${newStatus} shop`);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert(`An error occurred while ${newStatus}ing the shop`);
        });
    }
}

function deleteShop(shopId) {
    if (confirm('Are you sure you want to delete this shop? This action cannot be undone.')) {
        fetch(`/admin/shops/${shopId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert('Failed to delete shop');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while deleting the shop');
        });
    }
} 