// DOM Elements
const postsContainer = document.getElementById('postsContainer');
const pendingApprovals = document.getElementById('pendingApprovals');
const activeShopOwners = document.getElementById('activeShopOwners');
const recentPosts = document.getElementById('recentPosts');
const registrationForm = document.getElementById('registrationForm');
const postCreationForm = document.getElementById('postCreationForm');
const myPosts = document.getElementById('myPosts');
const shopOwnerPosts = document.getElementById('shopOwnerPosts');
const shopInfo = document.getElementById('shopInfo');

// Current shop owner (simulated login)
let currentShopOwner = null;

// Initialize the page based on current URL
document.addEventListener('DOMContentLoaded', () => {
    const currentPage = window.location.pathname.split('/').pop();
    
    if (currentPage === 'index.html' || currentPage === '') {
        loadHomePage();
    } else if (currentPage === 'admin.html') {
        loadAdminPage();
    } else if (currentPage === 'shop-owner.html') {
        loadShopOwnerPage();
    } else if (currentPage === 'shop-dashboard.html') {
        loadShopDashboard();
    }
});

// Home Page Functions
function loadHomePage() {
    if (postsContainer) {
        const posts = getAllPosts();
        displayPosts(posts, postsContainer);
    }
}

// Admin Page Functions
function loadAdminPage() {
    // Initialize navigation
    initializeAdminNavigation();

    // Load initial section
    const initialSection = window.location.hash || '#pending-approvals';
    showAdminSection(initialSection);
}

function initializeAdminNavigation() {
    const navLinks = document.querySelectorAll('.dashboard-nav .nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetSection = link.getAttribute('href');
            
            // Update active states
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
            
            // Show target section
            showAdminSection(targetSection);
            
            // Update URL hash
            window.location.hash = targetSection;
        });
    });
}

function showAdminSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.dashboard-section').forEach(section => {
        section.classList.remove('active');
    });

    // Show target section
    const targetSection = document.querySelector(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
        
        // Load section-specific content
        if (sectionId === '#pending-approvals') {
            loadPendingApprovals();
        } else if (sectionId === '#active-shops') {
            loadActiveShops();
        } else if (sectionId === '#recent-posts') {
            loadRecentPosts();
        } else if (sectionId === '#social-work') {
            loadSocialWorkSection();
        }
    }
}

function loadSocialWorkSection() {
    // Load donation posts
    const posts = getAllDonationPosts();
    const container = document.getElementById('donationPosts');
    
    if (container) {
        container.innerHTML = posts.map(post => `
            <div class="donation-card">
                <img src="${post.image}" alt="${post.title}" class="donation-image">
                <div class="donation-content">
                    <h4>${post.title}</h4>
                    <p class="donation-description">${post.description}</p>
                    <div class="donation-progress">
                        <div class="progress-bar">
                            <div class="progress" style="width: ${(post.currentAmount / post.targetAmount) * 100}%"></div>
                        </div>
                        <div class="donation-amounts">
                            <span>$${post.currentAmount} raised</span>
                            <span>of $${post.targetAmount}</span>
                        </div>
                    </div>
                    <p class="donation-date">Posted on: ${post.date}</p>
                    <div class="donation-actions">
                        <button class="btn btn-danger" onclick="deleteDonationPost(${post.id})">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // Handle donation form submission
    const donationForm = document.getElementById('donationForm');
    if (donationForm) {
        donationForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const newPost = {
                title: document.getElementById('donationTitle').value,
                description: document.getElementById('donationDescription').value,
                targetAmount: parseFloat(document.getElementById('donationTarget').value),
                image: document.getElementById('donationImage').value || 'https://via.placeholder.com/500x300'
            };

            if (addDonationPost(newPost)) {
                alert('Donation request created successfully!');
                loadSocialWorkSection();
                donationForm.reset();
            } else {
                alert('Failed to create donation request.');
            }
        });
    }
}

function loadPendingApprovals() {
    const pendingOwners = getPendingShopOwners();
    const container = document.getElementById('pendingApprovals');
    
    if (container) {
        container.innerHTML = pendingOwners.map(owner => `
            <div class="approval-card">
                <div class="approval-content">
                    <h4>${owner.shopName}</h4>
                    <p><i class="fas fa-user"></i> ${owner.ownerName}</p>
                    <p><i class="fas fa-envelope"></i> ${owner.email}</p>
                    <p><i class="fas fa-phone"></i> ${owner.phone}</p>
                    <p><i class="fas fa-tag"></i> ${owner.category}</p>
                    <p><i class="fas fa-map-marker-alt"></i> ${owner.address}</p>
                </div>
                <div class="approval-actions">
                    <button class="btn btn-success" onclick="approveShopOwner(${owner.id})">
                        <i class="fas fa-check"></i> Approve
                    </button>
                    <button class="btn btn-danger" onclick="rejectShopOwner(${owner.id})">
                        <i class="fas fa-times"></i> Reject
                    </button>
                </div>
            </div>
        `).join('');
    }
}

function loadActiveShops() {
    const approvedOwners = getApprovedShopOwners();
    const container = document.getElementById('activeShopOwners');
    
    if (container) {
        container.innerHTML = approvedOwners.map(owner => `
            <div class="shop-card">
                <div class="shop-content">
                    <h4>${owner.shopName}</h4>
                    <p><i class="fas fa-user"></i> ${owner.ownerName}</p>
                    <p><i class="fas fa-envelope"></i> ${owner.email}</p>
                    <p><i class="fas fa-tag"></i> ${owner.category}</p>
                    <p><i class="fas fa-box"></i> ${owner.posts.length} Posts</p>
                </div>
                <div class="shop-actions">
                    <button class="btn btn-danger" onclick="deleteShop(${owner.id})">
                        <i class="fas fa-trash"></i> Delete Shop
                    </button>
                </div>
            </div>
        `).join('');
    }
}

function loadRecentPosts() {
    const postsContainer = document.getElementById('postsContainer');
    const categoryFilter = document.getElementById('categoryFilter');
    const sortFilter = document.getElementById('sortFilter');

    // Get posts from data.js or API
    let posts = getPosts(); // This function should be defined in data.js

    // Apply filters
    categoryFilter.addEventListener('change', filterAndSortPosts);
    sortFilter.addEventListener('change', filterAndSortPosts);

    function filterAndSortPosts() {
        let filteredPosts = [...posts];

        // Apply category filter
        if (categoryFilter.value !== 'all') {
            filteredPosts = filteredPosts.filter(post => post.category === categoryFilter.value);
        }

        // Apply sort
        switch (sortFilter.value) {
            case 'newest':
                filteredPosts.sort((a, b) => new Date(b.date) - new Date(a.date));
                break;
            case 'oldest':
                filteredPosts.sort((a, b) => new Date(a.date) - new Date(b.date));
                break;
            case 'price-high':
                filteredPosts.sort((a, b) => b.price - a.price);
                break;
            case 'price-low':
                filteredPosts.sort((a, b) => a.price - b.price);
                break;
        }

        displayPosts(filteredPosts);
    }

    function displayPosts(postsToDisplay) {
        postsContainer.innerHTML = '';
        postsToDisplay.forEach(post => {
            const postCard = createPostCard(post);
            postsContainer.appendChild(postCard);
        });
    }

    function createPostCard(post) {
        const card = document.createElement('div');
        card.className = 'post-card';
        card.innerHTML = `
            <img src="${post.image}" alt="${post.title}" class="post-image">
            <div class="post-content">
                <h4>${post.title}</h4>
                <p class="post-price">$${post.price}</p>
                <p class="post-category">${post.category}</p>
                <p class="post-description">${post.description}</p>
                <p class="post-date">Posted: ${new Date(post.date).toLocaleDateString()}</p>
            </div>
        `;
        return card;
    }

    // Initial load
    filterAndSortPosts();
}

// Shop Owner Page Functions
function loadShopOwnerPage() {
    // Check if there's a current shop owner
    if (currentShopOwner) {
        registrationForm.style.display = 'none';
        postCreationForm.style.display = 'block';
        myPosts.style.display = 'block';
        
        const posts = getPostsByShopOwner(currentShopOwner.id);
        displayPosts(posts, shopOwnerPosts);
    } else {
        registrationForm.style.display = 'block';
        postCreationForm.style.display = 'none';
        myPosts.style.display = 'none';
    }
}

// Shop Dashboard Functions
function loadShopDashboard() {
    // Try to login as demo shop if no current shop owner
    if (!currentShopOwner) {
        currentShopOwner = loginAsDemoShop();
    }

    if (!currentShopOwner) {
        alert('Please register as a shop owner first.');
        window.location.href = 'shop-owner.html';
        return;
    }

    const owner = getShopOwnerById(currentShopOwner.id);
    if (!owner || owner.status !== 'approved') {
        alert('Your shop is not approved yet. Please wait for admin approval.');
        window.location.href = 'shop-owner.html';
        return;
    }

    // Display shop information in sidebar
    if (shopInfo) {
        shopInfo.innerHTML = `
            <div class="shop-details">
                <h4>${owner.shopName}</h4>
                <p><i class="fas fa-user"></i> ${owner.ownerName}</p>
                <p><i class="fas fa-envelope"></i> ${owner.email}</p>
                <p><i class="fas fa-phone"></i> ${owner.phone}</p>
                <p><i class="fas fa-tag"></i> ${owner.category}</p>
                <p><i class="fas fa-map-marker-alt"></i> ${owner.address}</p>
            </div>
        `;
    }

    // Initialize navigation
    initializeDashboardNavigation();

    // Set My Posts as default section
    const initialSection = window.location.hash || '#my-posts';
    showSection(initialSection);
    
    // Update active nav link
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === '#my-posts') {
            link.classList.add('active');
        }
    });
}

function initializeDashboardNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetSection = link.getAttribute('href');
            
            // Update active states
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
            
            // Show target section
            showSection(targetSection);
            
            // Update URL hash
            window.location.hash = targetSection;
        });
    });
}

function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.dashboard-section').forEach(section => {
        section.classList.remove('active');
    });

    // Show target section
    const targetSection = document.querySelector(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
        
        // Load section-specific content
        if (sectionId === '#my-posts') {
            loadPostsSection();
        } else if (sectionId === '#shop-settings') {
            loadShopSettingsSection();
        }
    }
}

function loadPostsSection() {
    const posts = getPostsByShopOwner(currentShopOwner.id);
    displayShopOwnerPosts(posts, shopOwnerPosts);
}

function loadShopSettingsSection() {
    const owner = getShopOwnerById(currentShopOwner.id);
    const settingsForm = document.getElementById('shopSettingsForm');
    
    if (settingsForm) {
        settingsForm.innerHTML = `
            <form id="editShopForm" class="edit-form">
                <div class="form-group">
                    <label for="editShopName">Shop Name</label>
                    <input type="text" id="editShopName" value="${owner.shopName}" required>
                </div>
                <div class="form-group">
                    <label for="editOwnerName">Owner Name</label>
                    <input type="text" id="editOwnerName" value="${owner.ownerName}" required>
                </div>
                <div class="form-group">
                    <label for="editEmail">Email</label>
                    <input type="email" id="editEmail" value="${owner.email}" required>
                </div>
                <div class="form-group">
                    <label for="editPhone">Phone</label>
                    <input type="tel" id="editPhone" value="${owner.phone}" required>
                </div>
                <div class="form-group">
                    <label for="editAddress">Address</label>
                    <input type="text" id="editAddress" value="${owner.address}" required>
                </div>
                <div class="form-group">
                    <label for="editCategory">Category</label>
                    <select id="editCategory" required>
                        <option value="electronics" ${owner.category === 'electronics' ? 'selected' : ''}>Electronics</option>
                        <option value="home" ${owner.category === 'home' ? 'selected' : ''}>Home & Garden</option>
                        <option value="vehicles" ${owner.category === 'vehicles' ? 'selected' : ''}>Vehicles</option>
                        <option value="fashion" ${owner.category === 'fashion' ? 'selected' : ''}>Fashion</option>
                    </select>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Save Changes</button>
                </div>
            </form>
        `;

        document.getElementById('editShopForm').addEventListener('submit', (e) => {
            e.preventDefault();
            const updatedInfo = {
                shopName: document.getElementById('editShopName').value,
                ownerName: document.getElementById('editOwnerName').value,
                email: document.getElementById('editEmail').value,
                phone: document.getElementById('editPhone').value,
                address: document.getElementById('editAddress').value,
                category: document.getElementById('editCategory').value
            };

            if (editShopInfo(currentShopOwner.id, updatedInfo)) {
                alert('Shop information updated successfully!');
                loadShopDashboard();
            } else {
                alert('Failed to update shop information.');
            }
        });
    }
}

// Event Listeners
if (document.getElementById('shopOwnerForm')) {
    document.getElementById('shopOwnerForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = {
            shopName: document.getElementById('shopName').value,
            ownerName: document.getElementById('ownerName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            address: document.getElementById('address').value,
            category: document.getElementById('shopCategory').value
        };
        
        const newShopOwner = addShopOwner(formData);
        currentShopOwner = newShopOwner;
        alert('Registration submitted successfully! Waiting for admin approval.');
        loadShopOwnerPage();
    });
}

if (document.getElementById('newPostForm')) {
    document.getElementById('newPostForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = {
            title: document.getElementById('postTitle').value,
            category: document.getElementById('postCategory').value,
            price: parseFloat(document.getElementById('postPrice').value),
            description: document.getElementById('postDescription').value,
            image: document.getElementById('postImage').value || 'https://via.placeholder.com/500x300'
        };
        
        addPost(currentShopOwner.id, formData);
        alert('Post created successfully!');
        loadShopDashboard();
    });
}

// Display Functions
function displayPosts(posts, container) {
    if (!container) return;
    
    container.innerHTML = posts.map(post => `
        <div class="post-card">
            <img src="${post.image}" alt="${post.title}" class="post-image">
            <div class="post-content">
                <h4 class="post-title">${post.title}</h4>
                <p class="post-price">$${post.price}</p>
                <p class="post-location">${post.category}</p>
            </div>
        </div>
    `).join('');
}

function displayShopOwnerPosts(posts, container) {
    if (!container) return;
    
    container.innerHTML = posts.map(post => `
        <div class="post-card">
            <img src="${post.image}" alt="${post.title}" class="post-image">
            <div class="post-content">
                <h4 class="post-title">${post.title}</h4>
                <p class="post-price">$${post.price}</p>
                <p class="post-location">${post.category}</p>
                <p class="post-description">${post.description}</p>
                <p class="post-date">Posted on: ${post.date}</p>
                <div class="post-actions">
                    <button class="btn btn-edit" onclick="editPostForm(${post.id})">Edit</button>
                    <button class="btn btn-danger" onclick="deletePost(${post.id})">Delete</button>
                </div>
            </div>
        </div>
    `).join('');
}

// Admin Actions
window.approveShopOwner = function(shopOwnerId) {
    approveShopOwner(shopOwnerId);
    loadAdminPage();
    alert('Shop owner approved successfully!');
};

window.rejectShopOwner = function(shopOwnerId) {
    rejectShopOwner(shopOwnerId);
    loadAdminPage();
    alert('Shop owner rejected successfully!');
};

window.deleteShop = function(shopOwnerId) {
    if (confirm('Are you sure you want to delete this shop? This action cannot be undone.')) {
        if (deleteShopOwner(shopOwnerId)) {
            alert('Shop deleted successfully!');
            loadActiveShops();
        } else {
            alert('Failed to delete shop.');
        }
    }
};

// Shop Dashboard Actions
window.deletePost = function(postId) {
    if (confirm('Are you sure you want to delete this post?')) {
        if (deletePost(currentShopOwner.id, postId)) {
            alert('Post deleted successfully!');
            loadShopDashboard();
        } else {
            alert('Failed to delete post.');
        }
    }
};

// Edit Post Form
window.editPostForm = function(postId) {
    const post = getPostsByShopOwner(currentShopOwner.id).find(p => p.id === postId);
    if (!post) return;

    const form = `
        <form id="editPostForm" class="edit-form">
            <h4>Edit Post</h4>
            <div class="form-group">
                <label for="editPostTitle">Title</label>
                <input type="text" id="editPostTitle" value="${post.title}" required>
            </div>
            <div class="form-group">
                <label for="editPostCategory">Category</label>
                <select id="editPostCategory" required>
                    <option value="electronics" ${post.category === 'electronics' ? 'selected' : ''}>Electronics</option>
                    <option value="home" ${post.category === 'home' ? 'selected' : ''}>Home & Garden</option>
                    <option value="vehicles" ${post.category === 'vehicles' ? 'selected' : ''}>Vehicles</option>
                    <option value="fashion" ${post.category === 'fashion' ? 'selected' : ''}>Fashion</option>
                </select>
            </div>
            <div class="form-group">
                <label for="editPostPrice">Price</label>
                <input type="number" id="editPostPrice" value="${post.price}" step="0.01" required>
            </div>
            <div class="form-group">
                <label for="editPostDescription">Description</label>
                <textarea id="editPostDescription" required>${post.description}</textarea>
            </div>
            <div class="form-group">
                <label for="editPostImage">Image URL</label>
                <input type="url" id="editPostImage" value="${post.image}">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Save Changes</button>
                <button type="button" class="btn btn-secondary" onclick="loadShopDashboard()">Cancel</button>
            </div>
        </form>
    `;

    const postCard = document.querySelector(`.post-card[data-post-id="${postId}"]`);
    if (postCard) {
        postCard.querySelector('.post-content').innerHTML = form;

        document.getElementById('editPostForm').addEventListener('submit', (e) => {
            e.preventDefault();
            const updatedPost = {
                title: document.getElementById('editPostTitle').value,
                category: document.getElementById('editPostCategory').value,
                price: parseFloat(document.getElementById('editPostPrice').value),
                description: document.getElementById('editPostDescription').value,
                image: document.getElementById('editPostImage').value || 'https://via.placeholder.com/500x300'
            };

            if (editPost(currentShopOwner.id, postId, updatedPost)) {
                alert('Post updated successfully!');
                loadShopDashboard();
            } else {
                alert('Failed to update post.');
            }
        });
    }
};

window.togglePostVisibility = function(postId) {
    const post = allPosts.find(p => p.id === postId);
    if (post) {
        if (post.hidden) {
            if (unhidePost(postId)) {
                alert('Post is now visible!');
                loadRecentPosts();
            } else {
                alert('Failed to show post.');
            }
        } else {
            if (hidePost(postId)) {
                alert('Post is now hidden!');
                loadRecentPosts();
            } else {
                alert('Failed to hide post.');
            }
        }
    }
};

window.deletePostAdmin = function(postId) {
    if (confirm('Are you sure you want to delete this post? This action cannot be undone.')) {
        // Find the shop owner who owns this post
        const owner = shopOwners.find(owner => 
            owner.posts.some(post => post.id === postId)
        );
        
        if (owner) {
            if (deletePost(owner.id, postId)) {
                alert('Post deleted successfully!');
                loadRecentPosts();
            } else {
                alert('Failed to delete post.');
            }
        }
    }
};

window.deleteDonationPost = function(postId) {
    if (confirm('Are you sure you want to delete this donation request? This action cannot be undone.')) {
        if (deleteDonationPost(postId)) {
            alert('Donation request deleted successfully!');
            loadSocialWorkSection();
        } else {
            alert('Failed to delete donation request.');
        }
    }
};

// User type detection and navigation setup
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in and get user type
    const userType = localStorage.getItem('userType'); // 'admin' or 'shop_owner'
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    // Update navigation based on user type
    const dashboardLink = document.getElementById('dashboardLink');
    const profileLink = document.getElementById('profileLink');
    const logoutLink = document.getElementById('logoutLink');

    if (isLoggedIn) {
        if (userType === 'admin') {
            dashboardLink.textContent = 'Admin Dashboard';
            dashboardLink.href = 'admin/shops.html';
        } else if (userType === 'shop_owner') {
            dashboardLink.textContent = 'Shop Dashboard';
            dashboardLink.href = 'posts.html';
        }
        // Show dashboard and profile links
        dashboardLink.style.display = 'block';
        profileLink.style.display = 'block';
        logoutLink.style.display = 'block';
    } else {
        // Hide dashboard and profile links if not logged in
        dashboardLink.style.display = 'none';
        profileLink.style.display = 'none';
        logoutLink.style.display = 'none';
    }

    // Handle logout
    logoutLink.addEventListener('click', function(e) {
        e.preventDefault();
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userType');
        window.location.href = 'index.html';
    });

    // Load recent posts
    loadRecentPosts();
    // Update stats
    updateStats();
});

// Function to handle login
function handleLogin(userType) {
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('userType', userType);
    // Redirect to homepage after login
    window.location.href = 'index.html';
}

// Example login functions (to be called from your login forms)
function loginAsAdmin() {
    handleLogin('admin');
}

function loginAsShopOwner() {
    handleLogin('shop_owner');
}

// Function to update stats
function updateStats() {
    // Get stats from data.js or API
    const stats = getStats(); // This function should be defined in data.js

    document.getElementById('activeShopsCount').textContent = stats.activeShops;
    document.getElementById('totalProductsCount').textContent = stats.totalProducts;
    document.getElementById('activeUsersCount').textContent = stats.activeUsers;
}

// Load More Posts functionality
document.querySelector('.load-more button').addEventListener('click', function() {
    // Implement pagination or load more posts logic here
    // This would typically involve making an API call to get more posts
    console.log('Load more posts clicked');
}); 